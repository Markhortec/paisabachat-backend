package com.hamza.paisabachat.backend.application.service;

import com.hamza.paisabachat.backend.infrastructure.persistence.entity.AchievementEntity;
import com.hamza.paisabachat.backend.infrastructure.persistence.entity.UserEntity;
import com.hamza.paisabachat.backend.infrastructure.persistence.repository.AchievementRepository;
import com.hamza.paisabachat.backend.infrastructure.persistence.repository.ContributionRepository;
import com.hamza.paisabachat.backend.infrastructure.persistence.repository.GoalRepository;
import com.hamza.paisabachat.backend.infrastructure.persistence.repository.UserRepository;
import com.hamza.paisabachat.backend.shared.constants.AchievementKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
public class GamificationService {

    private static final Logger log =
            LoggerFactory.getLogger(GamificationService.class);

    // ── XP Constants ──
    private static final long XP_PER_CONTRIBUTION_UNIT = 1L;
    private static final long XP_GOAL_COMPLETED = 1000L;
    private static final long XP_STREAK_DAILY = 50L;
    private static final long XP_ACHIEVEMENT_UNLOCK = 500L;

    private final UserRepository userRepository;
    private final AchievementRepository achievementRepository;
    private final ContributionRepository contributionRepository;
    private final GoalRepository goalRepository;
    private final StreakService streakService;

    public GamificationService(
            UserRepository userRepository,
            AchievementRepository achievementRepository,
            ContributionRepository contributionRepository,
            GoalRepository goalRepository,
            StreakService streakService) {
        this.userRepository = userRepository;
        this.achievementRepository = achievementRepository;
        this.contributionRepository = contributionRepository;
        this.goalRepository = goalRepository;
        this.streakService = streakService;
    }

    // ── Process Contribution Event ──
    @Async
    @Transactional
    public void onContributionAdded(String userId, BigDecimal amount) {
        try {
            // Update streak
            streakService.updateStreak(userId);

            // Award XP — amount / 100
            long xpEarned = amount.divide(
                    BigDecimal.valueOf(100),
                    0, java.math.RoundingMode.FLOOR).longValue();
            xpEarned = Math.max(xpEarned, XP_PER_CONTRIBUTION_UNIT);
            awardXp(userId, xpEarned);

            // Award streak XP
            awardXp(userId, XP_STREAK_DAILY);

            // Check achievements
            checkSavingsAchievements(userId);
            checkContributionAchievements(userId);
            checkStreakAchievements(userId);

        } catch (Exception e) {
            log.error("Error processing gamification for user: {} — {}",
                    userId, e.getMessage());
        }
    }

    // ── Process Goal Completed Event ──
    @Async
    @Transactional
    public void onGoalCompleted(String userId) {
        try {
            awardXp(userId, XP_GOAL_COMPLETED);
            checkGoalAchievements(userId);
        } catch (Exception e) {
            log.error("Error processing goal completion gamification: {}",
                    e.getMessage());
        }
    }

    // ── Award XP + Level Up ──
    @Transactional
    public void awardXp(String userId, long xp) {
        userRepository.findById(UUID.fromString(userId))
                .ifPresent(user -> {
                    long newXp = user.getXpTotal() + xp;
                    user.setXpTotal(newXp);
                    user.setLevel(calculateLevel(newXp));
                    userRepository.save(user);
                    log.debug("Awarded {} XP to user: {} — total: {}",
                            xp, userId, newXp);
                });
    }

    // ── Check Savings Achievements ──
    @Transactional
    public void checkSavingsAchievements(String userId) {
        UUID userUUID = UUID.fromString(userId);

        Optional<BigDecimal> totalSaved =
                contributionRepository.sumAmountByUserId(userUUID);

        if (totalSaved.isEmpty()) return;

        double total = totalSaved.get().doubleValue();

        checkAndUnlock(userId, AchievementKey.FIRST_STEP, total >= 1000);
        checkAndUnlock(userId, AchievementKey.RISING_SAVER, total >= 10000);
        checkAndUnlock(userId, AchievementKey.WEALTH_BUILDER, total >= 100000);
        checkAndUnlock(userId, AchievementKey.SAVINGS_KING, total >= 1000000);
    }

    // ── Check Streak Achievements ──
    @Transactional
    public void checkStreakAchievements(String userId) {
        int streak = streakService.getCurrentStreak(userId);
        checkAndUnlock(userId, AchievementKey.WEEK_WARRIOR, streak >= 7);
        checkAndUnlock(userId, AchievementKey.MONTH_MASTER, streak >= 30);
        checkAndUnlock(userId, AchievementKey.CENTURY_CLUB, streak >= 100);
    }

    // ── Check Goal Achievements ──
    @Transactional
    public void checkGoalAchievements(String userId) {
        UUID userUUID = UUID.fromString(userId);
        long completedGoals = goalRepository
                .countByUserIdAndStatus(userUUID, "COMPLETED");

        checkAndUnlock(userId, AchievementKey.GOAL_GETTER, completedGoals >= 1);
        checkAndUnlock(userId, AchievementKey.MULTI_TASKER, completedGoals >= 5);
        checkAndUnlock(userId, AchievementKey.DREAM_CHASER, completedGoals >= 10);
    }

    // ── Check Contribution Achievements ──
    @Transactional
    public void checkContributionAchievements(String userId) {
        UUID userUUID = UUID.fromString(userId);
        long totalContributions = contributionRepository
                .countByUserIdAndDate(userUUID);
        checkAndUnlock(userId, AchievementKey.FIRST_CONTRIBUTION,
                totalContributions >= 1);
    }

    // ── Get All Achievements ──
    @Transactional(readOnly = true)
    public java.util.List<AchievementEntity> getUserAchievements(String userId) {
        return achievementRepository.findByUserId(UUID.fromString(userId));
    }

    // ── Private: Check and Unlock Achievement ──
    private void checkAndUnlock(
            String userId, AchievementKey key, boolean condition) {
        if (!condition) return;

        UUID userUUID = UUID.fromString(userId);
        Optional<AchievementEntity> existing =
                achievementRepository.findByUserIdAndAchievementKey(
                        userUUID, key.name());

        if (existing.isPresent() && existing.get().getUnlocked()) {
            return; // Already unlocked
        }

        AchievementEntity achievement = existing.orElseGet(() -> {
            UserEntity user = userRepository.findById(userUUID).orElseThrow();
            return AchievementEntity.builder()
                    .user(user)
                    .achievementKey(key.name())
                    .build();
        });

        achievement.unlock();
        achievementRepository.save(achievement);

        // Award XP for achievement
        awardXp(userId, XP_ACHIEVEMENT_UNLOCK);

        log.info("Achievement unlocked: {} for user: {}", key.name(), userId);
    }

    // ── Private: Calculate Level from XP ──
    private int calculateLevel(long xp) {
        if (xp < 1000) return 1;
        if (xp < 5000) return 2;
        if (xp < 15000) return 3;
        if (xp < 35000) return 4;
        if (xp < 75000) return 5;
        if (xp < 150000) return 6;
        if (xp < 300000) return 7;
        if (xp < 600000) return 8;
        if (xp < 1000000) return 9;
        return 10;
    }
}