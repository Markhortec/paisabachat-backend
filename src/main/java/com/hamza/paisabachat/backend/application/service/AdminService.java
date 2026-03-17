package com.hamza.paisabachat.backend.application.service;

import com.hamza.paisabachat.backend.application.dto.response.UserResponse;
import com.hamza.paisabachat.backend.domain.exception.ResourceNotFoundException;
import com.hamza.paisabachat.backend.infrastructure.persistence.entity.UserEntity;
import com.hamza.paisabachat.backend.infrastructure.persistence.repository.AchievementRepository;
import com.hamza.paisabachat.backend.infrastructure.persistence.repository.ContributionRepository;
import com.hamza.paisabachat.backend.infrastructure.persistence.repository.GoalRepository;
import com.hamza.paisabachat.backend.infrastructure.persistence.repository.UserRepository;
import com.hamza.paisabachat.backend.shared.audit.AuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class AdminService {

    private static final Logger log =
            LoggerFactory.getLogger(AdminService.class);

    private final UserRepository userRepository;
    private final GoalRepository goalRepository;
    private final ContributionRepository contributionRepository;
    private final AchievementRepository achievementRepository;
    private final AuditService auditService;

    public AdminService(
            UserRepository userRepository,
            GoalRepository goalRepository,
            ContributionRepository contributionRepository,
            AchievementRepository achievementRepository,
            AuditService auditService) {
        this.userRepository = userRepository;
        this.goalRepository = goalRepository;
        this.contributionRepository = contributionRepository;
        this.achievementRepository = achievementRepository;
        this.auditService = auditService;
    }

    // ── Get All Users (Paginated) ──
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(int page, int size) {
        PageRequest pageRequest = PageRequest.of(
                page, size, Sort.by("createdAt").descending());
        return userRepository.findAll(pageRequest)
                .map(UserResponse::fromEntity);
    }

    // ── Get User By Id ──
    @Transactional(readOnly = true)
    public UserResponse getUserById(String userId) {
        UserEntity user = userRepository
                .findById(UUID.fromString(userId))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", userId));
        return UserResponse.fromEntity(user);
    }

    // ── Activate / Deactivate User ──
    public UserResponse toggleUserStatus(String adminId,
                                         String userId, boolean active) {
        UserEntity user = userRepository
                .findById(UUID.fromString(userId))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", userId));

        user.setIsActive(active);
        UserEntity updated = userRepository.save(user);

        auditService.logSimple(adminId,
                active ? "ADMIN_USER_ACTIVATED" : "ADMIN_USER_DEACTIVATED",
                "USER");

        log.info("Admin: {} {} user: {}",
                adminId, active ? "activated" : "deactivated", userId);

        return UserResponse.fromEntity(updated);
    }

    // ── Change User Tier ──
    public UserResponse changeUserTier(String adminId,
                                       String userId, String tier) {
        if (!tier.equals("FREE") && !tier.equals("PREMIUM")) {
            throw new com.hamza.paisabachat.backend.domain.exception
                    .BusinessException(
                    "Invalid tier. Must be FREE or PREMIUM.",
                    "INVALID_TIER");
        }

        UserEntity user = userRepository
                .findById(UUID.fromString(userId))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", userId));

        user.setTier(tier);
        UserEntity updated = userRepository.save(user);

        auditService.logSimple(adminId, "ADMIN_TIER_CHANGED", "USER");
        log.info("Admin: {} changed tier of user: {} to {}",
                adminId, userId, tier);

        return UserResponse.fromEntity(updated);
    }

    // ── Get Platform Stats ──
    @Transactional(readOnly = true)
    public Map<String, Object> getPlatformStats() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalUsers", userRepository.count());
        stats.put("freeUsers", userRepository.countByTier("FREE"));
        stats.put("premiumUsers", userRepository.countByTier("PREMIUM"));
        stats.put("totalGoals", goalRepository.count());
        stats.put("totalContributions", contributionRepository.count());
        stats.put("totalAchievements", achievementRepository.count());

        return stats;
    }
}