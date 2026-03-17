package com.hamza.paisabachat.backend.infrastructure.scheduler;

import com.hamza.paisabachat.backend.application.service.StreakService;
import com.hamza.paisabachat.backend.infrastructure.notification.NotificationService;
import com.hamza.paisabachat.backend.infrastructure.persistence.repository.RefreshTokenRepository;
import com.hamza.paisabachat.backend.infrastructure.persistence.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SchedulerService {

    private static final Logger log =
            LoggerFactory.getLogger(SchedulerService.class);

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final StreakService streakService;
    private final NotificationService notificationService;

    public SchedulerService(
            RefreshTokenRepository refreshTokenRepository,
            UserRepository userRepository,
            StreakService streakService,
            NotificationService notificationService) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.streakService = streakService;
        this.notificationService = notificationService;
    }

    // ── Clean Expired Tokens — Every day at 2 AM ──
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanExpiredTokens() {
        log.info("Cleaning expired refresh tokens...");
        refreshTokenRepository.deleteExpiredAndRevokedTokens(
                LocalDateTime.now());
        log.info("Expired tokens cleaned successfully");
    }

    // ── Reset Missed Streaks — Every day at midnight ──
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void resetMissedStreaks() {
        log.info("Checking for missed streaks...");
        List<String> allUserIds = userRepository.findAll()
                .stream()
                .filter(u -> u.getStreakCurrent() > 0)
                .map(u -> u.getId().toString())
                .toList();

        allUserIds.forEach(userId -> {
            try {
                streakService.resetStreakIfMissed(userId);
            } catch (Exception e) {
                log.error("Error resetting streak for user: {} — {}",
                        userId, e.getMessage());
            }
        });

        log.info("Streak check complete for {} users", allUserIds.size());
    }

    // ── Send Daily Reminders — Every hour at minute 0 ──
    @Scheduled(cron = "0 0 * * * *")
    public void sendDailyReminders() {
        log.info("Sending scheduled daily reminders...");
        try {
            notificationService.sendDailyReminders();
        } catch (Exception e) {
            log.error("Error sending daily reminders: {}", e.getMessage());
        }
    }

    // ── Send Streak At Risk Alerts — Every day at 8 PM ──
    @Scheduled(cron = "0 0 20 * * *")
    @Transactional(readOnly = true)
    public void sendStreakAtRiskAlerts() {
        log.info("Checking streak at risk users...");
        userRepository.findAll()
                .stream()
                .filter(u -> u.getStreakCurrent() > 0)
                .forEach(user -> {
                    String userId = user.getId().toString();
                    try {
                        if (streakService.isStreakAtRisk(userId)) {
                            notificationService.sendStreakReminderNotification(
                                    userId, user.getStreakCurrent());
                        }
                    } catch (Exception e) {
                        log.error("Error checking streak risk for user: {}",
                                userId);
                    }
                });
    }
}