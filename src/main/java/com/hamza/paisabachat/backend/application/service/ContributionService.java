package com.hamza.paisabachat.backend.application.service;

import com.hamza.paisabachat.backend.application.dto.request.CreateContributionRequest;
import com.hamza.paisabachat.backend.application.dto.request.UpdateContributionRequest;
import com.hamza.paisabachat.backend.application.dto.response.ContributionResponse;
import com.hamza.paisabachat.backend.domain.exception.BusinessException;
import com.hamza.paisabachat.backend.domain.exception.ResourceNotFoundException;
import com.hamza.paisabachat.backend.infrastructure.persistence.entity.ContributionEntity;
import com.hamza.paisabachat.backend.infrastructure.persistence.entity.GoalEntity;
import com.hamza.paisabachat.backend.infrastructure.persistence.entity.UserEntity;
import com.hamza.paisabachat.backend.infrastructure.persistence.repository.ContributionRepository;
import com.hamza.paisabachat.backend.infrastructure.persistence.repository.GoalRepository;
import com.hamza.paisabachat.backend.infrastructure.persistence.repository.UserRepository;
import com.hamza.paisabachat.backend.shared.audit.AuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ContributionService {

    private static final Logger log =
            LoggerFactory.getLogger(ContributionService.class);

    private final ContributionRepository contributionRepository;
    private final GoalRepository goalRepository;
    private final UserRepository userRepository;
    private final GoalService goalService;
    private final AuditService auditService;
    private final GamificationService gamificationService;

    public ContributionService(
            ContributionRepository contributionRepository,
            GoalRepository goalRepository,
            UserRepository userRepository,
            GoalService goalService,
            AuditService auditService,
            GamificationService gamificationService) {
        this.contributionRepository = contributionRepository;
        this.goalRepository = goalRepository;
        this.userRepository = userRepository;
        this.goalService = goalService;
        this.auditService = auditService;
        this.gamificationService = gamificationService;
    }

    // ── Add Contribution ──
    public ContributionResponse addContribution(
            String userId, CreateContributionRequest request) {

        UserEntity user = findUserById(userId);
        GoalEntity goal = findGoalByIdAndUserId(
                request.getGoalId(), userId);

        if (!"ACTIVE".equals(goal.getStatus())) {
            throw new BusinessException(
                    "Cannot add contribution to a non-active goal.",
                    "GOAL_NOT_ACTIVE"
            );
        }

        LocalDate contributionDate = request.getContributedAt() != null
                ? request.getContributedAt()
                : LocalDate.now();

        ContributionEntity contribution = ContributionEntity.builder()
                .goal(goal)
                .user(user)
                .amount(request.getAmount())
                .note(request.getNote())
                .contributedAt(contributionDate)
                .build();

        contributionRepository.save(contribution);

        // Update goal saved amount
        BigDecimal newSavedAmount = goal.getSavedAmount()
                .add(request.getAmount());
        goal.setSavedAmount(newSavedAmount);
        goalRepository.save(goal);

        // Check if goal is completed
        if (goal.isCompleted()) {
            goalService.markGoalCompleted(goal, userId);
            gamificationService.onGoalCompleted(userId);
            log.info("Goal completed after contribution: {}", goal.getId());
        }

        // Gamification — streak + XP + achievements
        gamificationService.onContributionAdded(userId, request.getAmount());

        auditService.logSimple(userId, "CONTRIBUTION_ADDED", "CONTRIBUTION");
        log.info("Contribution added: {} PKR to goal: {} by user: {}",
                request.getAmount(), goal.getId(), userId);

        return ContributionResponse.fromEntity(contribution);
    }

    // ── Get Contributions by Goal ──
    @Transactional(readOnly = true)
    public List<ContributionResponse> getContributionsByGoal(
            String userId, UUID goalId) {
        findGoalByIdAndUserId(goalId, userId);
        return contributionRepository
                .findByGoalIdOrderByContributedAtDesc(goalId)
                .stream()
                .map(ContributionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // ── Get All User Contributions ──
    @Transactional(readOnly = true)
    public List<ContributionResponse> getAllContributions(String userId) {
        return contributionRepository
                .findByUserIdOrderByContributedAtDesc(UUID.fromString(userId))
                .stream()
                .map(ContributionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // ── Get Contribution By Id ──
    @Transactional(readOnly = true)
    public ContributionResponse getContributionById(
            String userId, UUID contributionId) {
        ContributionEntity contribution = findContributionByIdAndUserId(
                contributionId, userId);
        return ContributionResponse.fromEntity(contribution);
    }

    // ── Update Contribution ──
    public ContributionResponse updateContribution(
            String userId, UUID contributionId,
            UpdateContributionRequest request) {

        ContributionEntity contribution = findContributionByIdAndUserId(
                contributionId, userId);

        GoalEntity goal = contribution.getGoal();

        if (request.getAmount() != null) {
            BigDecimal oldAmount = contribution.getAmount();
            BigDecimal newAmount = request.getAmount();
            BigDecimal difference = newAmount.subtract(oldAmount);

            BigDecimal newSavedAmount = goal.getSavedAmount().add(difference);
            if (newSavedAmount.compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException(
                        "Update would result in negative saved amount.",
                        "INVALID_UPDATE"
                );
            }

            goal.setSavedAmount(newSavedAmount);
            goalRepository.save(goal);
            contribution.setAmount(newAmount);
        }

        if (request.getNote() != null) {
            contribution.setNote(request.getNote());
        }

        if (request.getContributedAt() != null) {
            contribution.setContributedAt(request.getContributedAt());
        }

        ContributionEntity updated = contributionRepository.save(contribution);
        auditService.logSimple(userId, "CONTRIBUTION_UPDATED", "CONTRIBUTION");
        log.info("Contribution updated: {} by user: {}", contributionId, userId);

        return ContributionResponse.fromEntity(updated);
    }

    // ── Delete Contribution ──
    public void deleteContribution(String userId, UUID contributionId) {
        ContributionEntity contribution = findContributionByIdAndUserId(
                contributionId, userId);

        GoalEntity goal = contribution.getGoal();

        BigDecimal newSavedAmount = goal.getSavedAmount()
                .subtract(contribution.getAmount())
                .max(BigDecimal.ZERO);
        goal.setSavedAmount(newSavedAmount);

        if ("COMPLETED".equals(goal.getStatus())) {
            goal.setStatus("ACTIVE");
        }

        goalRepository.save(goal);
        contributionRepository.delete(contribution);

        auditService.logSimple(userId, "CONTRIBUTION_DELETED", "CONTRIBUTION");
        log.info("Contribution deleted: {} by user: {}", contributionId, userId);
    }

    // ── Get Contributions by Date Range ──
    @Transactional(readOnly = true)
    public List<ContributionResponse> getContributionsByDateRange(
            String userId, LocalDate startDate, LocalDate endDate) {

        if (startDate.isAfter(endDate)) {
            throw new BusinessException(
                    "Start date cannot be after end date.",
                    "INVALID_DATE_RANGE"
            );
        }

        return contributionRepository
                .findByUserIdAndDateRange(
                        UUID.fromString(userId), startDate, endDate)
                .stream()
                .map(ContributionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // ── Private Helpers ──
    private UserEntity findUserById(String userId) {
        return userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", userId));
    }

    private GoalEntity findGoalByIdAndUserId(UUID goalId, String userId) {
        return goalRepository
                .findByIdAndUserId(goalId, UUID.fromString(userId))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Goal", goalId.toString()));
    }

    private ContributionEntity findContributionByIdAndUserId(
            UUID contributionId, String userId) {
        return contributionRepository
                .findByIdAndUserId(contributionId, UUID.fromString(userId))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Contribution", contributionId.toString()));
    }
}