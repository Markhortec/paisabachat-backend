package com.hamza.paisabachat.backend.application.service;

import com.hamza.paisabachat.backend.application.dto.request.CreateGoalRequest;
import com.hamza.paisabachat.backend.application.dto.request.UpdateGoalRequest;
import com.hamza.paisabachat.backend.application.dto.response.GoalResponse;
import com.hamza.paisabachat.backend.application.dto.response.GoalSummaryResponse;
import com.hamza.paisabachat.backend.domain.exception.BusinessException;
import com.hamza.paisabachat.backend.domain.exception.ResourceNotFoundException;
import com.hamza.paisabachat.backend.infrastructure.persistence.entity.GoalEntity;
import com.hamza.paisabachat.backend.infrastructure.persistence.entity.UserEntity;
import com.hamza.paisabachat.backend.infrastructure.persistence.repository.GoalRepository;
import com.hamza.paisabachat.backend.infrastructure.persistence.repository.UserRepository;
import com.hamza.paisabachat.backend.shared.audit.AuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class GoalService {

    private static final Logger log = LoggerFactory.getLogger(GoalService.class);

    private static final int FREE_TIER_GOAL_LIMIT = 3;
    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String STATUS_ARCHIVED = "ARCHIVED";

    private final GoalRepository goalRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    public GoalService(
            GoalRepository goalRepository,
            UserRepository userRepository,
            AuditService auditService) {
        this.goalRepository = goalRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    // ── Create Goal ──
    public GoalResponse createGoal(String userId, CreateGoalRequest request) {
        UserEntity user = findUserById(userId);

        // Free tier limit check
        if ("FREE".equals(user.getTier())) {
            long activeGoals = goalRepository.countActiveGoalsByUserId(
                    UUID.fromString(userId));
            if (activeGoals >= FREE_TIER_GOAL_LIMIT) {
                throw BusinessException.goalLimitReached();
            }
        }

        GoalEntity goal = GoalEntity.builder()
                .user(user)
                .title(request.getTitle().trim())
                .description(request.getDescription())
                .targetAmount(request.getTargetAmount())
                .deadline(request.getDeadline())
                .priority(request.getPriority())
                .iconName(request.getIconName())
                .build();

        GoalEntity saved = goalRepository.save(goal);

        auditService.logSimple(userId, "GOAL_CREATED", "GOAL");
        log.info("Goal created: {} for user: {}", saved.getId(), userId);

        return GoalResponse.fromEntity(saved);
    }

    // ── Get All Goals ──
    @Transactional(readOnly = true)
    public List<GoalResponse> getAllGoals(String userId) {
        return goalRepository
                .findByUserIdOrderByCreatedAtDesc(UUID.fromString(userId))
                .stream()
                .map(GoalResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // ── Get Active Goals ──
    @Transactional(readOnly = true)
    public List<GoalResponse> getActiveGoals(String userId) {
        return goalRepository
                .findActiveGoalsByUserIdOrderByDeadline(UUID.fromString(userId))
                .stream()
                .map(GoalResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // ── Get Goal By Id ──
    @Transactional(readOnly = true)
    public GoalResponse getGoalById(String userId, UUID goalId) {
        GoalEntity goal = findGoalByIdAndUserId(goalId, userId);
        return GoalResponse.fromEntity(goal);
    }

    // ── Update Goal ──
    public GoalResponse updateGoal(
            String userId, UUID goalId, UpdateGoalRequest request) {
        GoalEntity goal = findGoalByIdAndUserId(goalId, userId);

        if (STATUS_COMPLETED.equals(goal.getStatus())) {
            throw BusinessException.goalAlreadyCompleted();
        }

        if (request.getTitle() != null) {
            goal.setTitle(request.getTitle().trim());
        }
        if (request.getDescription() != null) {
            goal.setDescription(request.getDescription());
        }
        if (request.getDeadline() != null) {
            goal.setDeadline(request.getDeadline());
        }
        if (request.getPriority() != null) {
            goal.setPriority(request.getPriority());
        }
        if (request.getIconName() != null) {
            goal.setIconName(request.getIconName());
        }

        GoalEntity updated = goalRepository.save(goal);
        auditService.logSimple(userId, "GOAL_UPDATED", "GOAL");
        log.info("Goal updated: {} by user: {}", goalId, userId);

        return GoalResponse.fromEntity(updated);
    }

    // ── Delete Goal ──
    public void deleteGoal(String userId, UUID goalId) {
        GoalEntity goal = findGoalByIdAndUserId(goalId, userId);
        goalRepository.delete(goal);
        auditService.logSimple(userId, "GOAL_DELETED", "GOAL");
        log.info("Goal deleted: {} by user: {}", goalId, userId);
    }

    // ── Archive Goal ──
    public GoalResponse archiveGoal(String userId, UUID goalId) {
        GoalEntity goal = findGoalByIdAndUserId(goalId, userId);
        goal.setStatus(STATUS_ARCHIVED);
        GoalEntity archived = goalRepository.save(goal);
        auditService.logSimple(userId, "GOAL_ARCHIVED", "GOAL");
        log.info("Goal archived: {} by user: {}", goalId, userId);
        return GoalResponse.fromEntity(archived);
    }

    // ── Get Goal Summary ──
    @Transactional(readOnly = true)
    public GoalSummaryResponse getGoalSummary(String userId) {
        UUID userUUID = UUID.fromString(userId);

        List<GoalEntity> allGoals = goalRepository
                .findByUserIdOrderByCreatedAtDesc(userUUID);

        List<GoalResponse> goalResponses = allGoals.stream()
                .map(GoalResponse::fromEntity)
                .collect(Collectors.toList());

        long activeCount = allGoals.stream()
                .filter(g -> STATUS_ACTIVE.equals(g.getStatus()))
                .count();

        long completedCount = allGoals.stream()
                .filter(g -> STATUS_COMPLETED.equals(g.getStatus()))
                .count();

        long archivedCount = allGoals.stream()
                .filter(g -> STATUS_ARCHIVED.equals(g.getStatus()))
                .count();

        BigDecimal totalSaved = allGoals.stream()
                .map(GoalEntity::getSavedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalTarget = allGoals.stream()
                .map(GoalEntity::getTargetAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        double overallProgress = totalTarget.compareTo(BigDecimal.ZERO) > 0
                ? totalSaved.divide(totalTarget, 4,
                        java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue()
                : 0.0;

        return GoalSummaryResponse.builder()
                .totalGoals(allGoals.size())
                .activeGoals(activeCount)
                .completedGoals(completedCount)
                .archivedGoals(archivedCount)
                .totalSaved(totalSaved)
                .totalTarget(totalTarget)
                .overallProgress(overallProgress)
                .goals(goalResponses)
                .build();
    }

    // ── Internal: Mark Goal Complete ──
    public void markGoalCompleted(GoalEntity goal, String userId) {
        goal.setStatus(STATUS_COMPLETED);
        goalRepository.save(goal);
        auditService.logSimple(userId, "GOAL_COMPLETED", "GOAL");
        log.info("Goal completed: {} by user: {}", goal.getId(), userId);
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
}