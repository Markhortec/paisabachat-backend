package com.hamza.paisabachat.backend.application.service;

import com.hamza.paisabachat.backend.infrastructure.persistence.entity.UserEntity;
import com.hamza.paisabachat.backend.infrastructure.persistence.repository.ContributionRepository;
import com.hamza.paisabachat.backend.infrastructure.persistence.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
@Transactional
public class StreakService {

    private static final Logger log = LoggerFactory.getLogger(StreakService.class);

    private final UserRepository userRepository;
    private final ContributionRepository contributionRepository;

    public StreakService(
            UserRepository userRepository,
            ContributionRepository contributionRepository) {
        this.userRepository = userRepository;
        this.contributionRepository = contributionRepository;
    }

    // ── Update Streak After Contribution ──
    public void updateStreak(String userId) {
        UUID userUUID = UUID.fromString(userId);
        UserEntity user = userRepository.findById(userUUID)
                .orElse(null);

        if (user == null) return;

        LocalDate today = LocalDate.now();
        LocalDate lastDate = user.getStreakLastDate();

        if (lastDate == null) {
            // First ever contribution
            user.setStreakCurrent(1);
            user.setStreakLongest(1);
            user.setStreakLastDate(today);

        } else if (lastDate.equals(today)) {
            // Already contributed today — no change
            return;

        } else if (lastDate.equals(today.minusDays(1))) {
            // Consecutive day — increment streak
            int newStreak = user.getStreakCurrent() + 1;
            user.setStreakCurrent(newStreak);
            user.setStreakLastDate(today);

            // Update longest streak if needed
            if (newStreak > user.getStreakLongest()) {
                user.setStreakLongest(newStreak);
            }

        } else {
            // Streak broken — reset
            log.info("Streak reset for user: {} (last: {}, today: {})",
                    userId, lastDate, today);
            user.setStreakCurrent(1);
            user.setStreakLastDate(today);
        }

        userRepository.save(user);
        log.info("Streak updated for user: {} — current: {}",
                userId, user.getStreakCurrent());
    }

    // ── Get Current Streak ──
    @Transactional(readOnly = true)
    public int getCurrentStreak(String userId) {
        return userRepository.findById(UUID.fromString(userId))
                .map(UserEntity::getStreakCurrent)
                .orElse(0);
    }

    // ── Get Longest Streak ──
    @Transactional(readOnly = true)
    public int getLongestStreak(String userId) {
        return userRepository.findById(UUID.fromString(userId))
                .map(UserEntity::getStreakLongest)
                .orElse(0);
    }

    // ── Check If Streak At Risk ──
    @Transactional(readOnly = true)
    public boolean isStreakAtRisk(String userId) {
        UUID userUUID = UUID.fromString(userId);
        UserEntity user = userRepository.findById(userUUID)
                .orElse(null);

        if (user == null || user.getStreakCurrent() == 0) return false;

        LocalDate today = LocalDate.now();
        LocalDate lastDate = user.getStreakLastDate();

        if (lastDate == null) return false;

        // Streak at risk if last contribution was yesterday
        // and today not yet contributed
        return lastDate.equals(today.minusDays(1))
                && !contributionRepository.existsByUserIdAndDate(
                userUUID, today);
    }

    // ── Reset Streak (called by scheduler) ──
    public void resetStreakIfMissed(String userId) {
        UUID userUUID = UUID.fromString(userId);
        UserEntity user = userRepository.findById(userUUID)
                .orElse(null);

        if (user == null || user.getStreakCurrent() == 0) return;

        LocalDate today = LocalDate.now();
        LocalDate lastDate = user.getStreakLastDate();

        if (lastDate == null) return;

        // If last contribution was before yesterday — reset streak
        if (lastDate.isBefore(today.minusDays(1))) {
            user.setStreakCurrent(0);
            userRepository.save(user);
            log.info("Streak reset for user: {} — missed day", userId);
        }
    }
}