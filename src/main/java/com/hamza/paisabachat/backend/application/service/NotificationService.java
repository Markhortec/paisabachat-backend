package com.hamza.paisabachat.backend.infrastructure.notification;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.*;
import com.hamza.paisabachat.backend.infrastructure.persistence.entity.NotificationSettingsEntity;
import com.hamza.paisabachat.backend.infrastructure.persistence.entity.UserEntity;
import com.hamza.paisabachat.backend.infrastructure.persistence.repository.NotificationSettingsRepository;
import com.hamza.paisabachat.backend.infrastructure.persistence.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class NotificationService {

    private static final Logger log =
            LoggerFactory.getLogger(NotificationService.class);

    private final NotificationSettingsRepository notificationSettingsRepository;
    private final UserRepository userRepository;

    public NotificationService(
            NotificationSettingsRepository notificationSettingsRepository,
            UserRepository userRepository) {
        this.notificationSettingsRepository = notificationSettingsRepository;
        this.userRepository = userRepository;
    }

    // ── Send Goal Completed Notification ──
    @Async
    public void sendGoalCompletedNotification(String userId, String goalTitle) {
        sendToUser(userId,
                "🎉 Goal Complete! Mubarak ho!",
                String.format("'%s' complete ho gaya! Aap amazing hain!",
                        goalTitle),
                "GOAL_COMPLETED");
    }

    // ── Send Streak Reminder ──
    @Async
    public void sendStreakReminderNotification(String userId, int currentStreak) {
        sendToUser(userId,
                "🔥 Streak Alert!",
                String.format("Aaj ki bachat add karein — " +
                        "%d din ki streak break na ho!", currentStreak),
                "STREAK_REMINDER");
    }

    // ── Send Achievement Unlocked ──
    @Async
    public void sendAchievementNotification(String userId,
                                            String achievementName) {
        sendToUser(userId,
                "🏆 Achievement Unlock!",
                String.format("Badhai ho! '%s' achievement unlock ho gayi!",
                        achievementName),
                "ACHIEVEMENT_UNLOCKED");
    }

    // ── Send Daily Reminder ──
    @Async
    @Transactional(readOnly = true)
    public void sendDailyReminders() {
        LocalTime now = LocalTime.now()
                .withSecond(0).withNano(0);

        List<NotificationSettingsEntity> users =
                notificationSettingsRepository.findUsersForReminder(now);

        log.info("Sending daily reminders to {} users", users.size());

        users.forEach(settings -> {
            if (settings.getFcmToken() != null) {
                sendToUser(
                        settings.getUser().getId().toString(),
                        "💰 Bachat Time!",
                        "Aaj ki savings add karein — " +
                                "apne goals ke kareeb aao!",
                        "DAILY_REMINDER");
            }
        });
    }

    // ── Update FCM Token ──
    @Transactional
    public void updateFcmToken(String userId, String fcmToken) {
        UUID userUUID = UUID.fromString(userId);

        NotificationSettingsEntity settings =
                notificationSettingsRepository.findByUserId(userUUID)
                        .orElseGet(() -> {
                            UserEntity user = userRepository
                                    .findById(userUUID).orElseThrow();
                            NotificationSettingsEntity newSettings =
                                    NotificationSettingsEntity.builder()
                                            .user(user)
                                            .fcmToken(fcmToken)
                                            .build();
                            return newSettings;
                        });

        settings.setFcmToken(fcmToken);
        notificationSettingsRepository.save(settings);
        log.info("FCM token updated for user: {}", userId);
    }

    // ── Get Notification Settings ──
    @Transactional(readOnly = true)
    public Optional<NotificationSettingsEntity> getSettings(String userId) {
        return notificationSettingsRepository
                .findByUserId(UUID.fromString(userId));
    }

    // ── Private: Send to User ──
    private void sendToUser(String userId, String title,
                            String body, String type) {
        if (FirebaseApp.getApps().isEmpty()) {
            log.debug("Firebase not available — skipping notification");
            return;
        }

        notificationSettingsRepository
                .findByUserId(UUID.fromString(userId))
                .filter(s -> s.getFcmToken() != null)
                .ifPresent(settings -> {
                    try {
                        Message message = Message.builder()
                                .setToken(settings.getFcmToken())
                                .setNotification(Notification.builder()
                                        .setTitle(title)
                                        .setBody(body)
                                        .build())
                                .putData("type", type)
                                .putData("userId", userId)
                                .build();

                        String response = FirebaseMessaging
                                .getInstance().send(message);
                        log.debug("Notification sent: {} — {}", type, response);

                    } catch (FirebaseMessagingException e) {
                        log.warn("Failed to send notification to user: {} — {}",
                                userId, e.getMessage());
                    }
                });
    }
}