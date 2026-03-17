package com.hamza.paisabachat.backend.application.service;

import com.hamza.paisabachat.backend.application.dto.response.AnalyticsResponse;
import com.hamza.paisabachat.backend.application.dto.response.UserResponse;
import com.hamza.paisabachat.backend.domain.exception.BusinessException;
import com.hamza.paisabachat.backend.domain.exception.ResourceNotFoundException;
import com.hamza.paisabachat.backend.infrastructure.persistence.entity.AchievementEntity;
import com.hamza.paisabachat.backend.infrastructure.persistence.entity.UserEntity;
import com.hamza.paisabachat.backend.infrastructure.persistence.repository.AchievementRepository;
import com.hamza.paisabachat.backend.infrastructure.persistence.repository.UserRepository;
import com.hamza.paisabachat.backend.shared.audit.AuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class UserProfileService {

    private static final Logger log =
            LoggerFactory.getLogger(UserProfileService.class);

    private final UserRepository userRepository;
    private final AchievementRepository achievementRepository;
    private final AnalyticsService analyticsService;
    private final AuditService auditService;

    public UserProfileService(
            UserRepository userRepository,
            AchievementRepository achievementRepository,
            AnalyticsService analyticsService,
            AuditService auditService) {
        this.userRepository = userRepository;
        this.achievementRepository = achievementRepository;
        this.analyticsService = analyticsService;
        this.auditService = auditService;
    }

    // ── Get Profile ──
    @Transactional(readOnly = true)
    public UserResponse getProfile(String userId) {
        UserEntity user = findUserById(userId);
        return UserResponse.fromEntity(user);
    }

    // ── Update Profile ──
    public UserResponse updateProfile(String userId, String name,
                                      String avatarUrl) {
        UserEntity user = findUserById(userId);

        if (name != null && !name.isBlank()) {
            if (name.length() < 2 || name.length() > 100) {
                throw new BusinessException(
                        "Name must be between 2 and 100 characters.",
                        "INVALID_NAME"
                );
            }
            user.setName(name.trim());
        }

        if (avatarUrl != null && !avatarUrl.isBlank()) {
            user.setAvatarUrl(avatarUrl);
        }

        UserEntity updated = userRepository.save(user);
        auditService.logSimple(userId, "PROFILE_UPDATED", "USER");
        log.info("Profile updated for user: {}", userId);

        return UserResponse.fromEntity(updated);
    }

    // ── Get Achievements ──
    @Transactional(readOnly = true)
    public List<AchievementEntity> getAchievements(String userId) {
        return achievementRepository.findByUserId(UUID.fromString(userId));
    }

    // ── Get Unlocked Achievements ──
    @Transactional(readOnly = true)
    public List<AchievementEntity> getUnlockedAchievements(String userId) {
        return achievementRepository
                .findUnlockedByUserIdOrderByDate(UUID.fromString(userId));
    }

    // ── Get Dashboard Summary ──
    @Transactional(readOnly = true)
    public DashboardSummary getDashboardSummary(String userId) {
        UserEntity user = findUserById(userId);
        AnalyticsResponse analytics = analyticsService.getAnalytics(userId);
        long unlockedAchievements = achievementRepository
                .countByUserIdAndUnlocked(UUID.fromString(userId), true);

        return new DashboardSummary(
                UserResponse.fromEntity(user),
                analytics,
                unlockedAchievements
        );
    }

    // ── Deactivate Account ──
    public void deactivateAccount(String userId) {
        UserEntity user = findUserById(userId);
        user.setIsActive(false);
        userRepository.save(user);
        auditService.logSimple(userId, "ACCOUNT_DEACTIVATED", "USER");
        log.info("Account deactivated: {}", userId);
    }

    // ── Upgrade to Premium ──
    public UserResponse upgradeToPremium(String userId) {
        UserEntity user = findUserById(userId);
        user.setTier("PREMIUM");
        UserEntity updated = userRepository.save(user);
        auditService.logSimple(userId, "UPGRADED_TO_PREMIUM", "USER");
        log.info("User upgraded to premium: {}", userId);
        return UserResponse.fromEntity(updated);
    }

    // ── Private Helpers ──
    private UserEntity findUserById(String userId) {
        return userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", userId));
    }

    // ── Dashboard Summary ──
    public static class DashboardSummary {
        private final UserResponse user;
        private final AnalyticsResponse analytics;
        private final long unlockedAchievements;

        public DashboardSummary(UserResponse user,
                                AnalyticsResponse analytics,
                                long unlockedAchievements) {
            this.user = user;
            this.analytics = analytics;
            this.unlockedAchievements = unlockedAchievements;
        }

        public UserResponse getUser() { return user; }
        public AnalyticsResponse getAnalytics() { return analytics; }
        public long getUnlockedAchievements() { return unlockedAchievements; }
    }
}