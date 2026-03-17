package com.hamza.paisabachat.backend.infrastructure.persistence.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "notification_settings")
public class NotificationSettingsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserEntity user;

    @Column(name = "daily_reminder_enabled", nullable = false)
    private Boolean dailyReminderEnabled = true;

    @Column(name = "daily_reminder_time", nullable = false)
    private LocalTime dailyReminderTime = LocalTime.of(20, 0);

    @Column(name = "streak_alerts_enabled", nullable = false)
    private Boolean streakAlertsEnabled = true;

    @Column(name = "goal_alerts_enabled", nullable = false)
    private Boolean goalAlertsEnabled = true;

    @Column(name = "fcm_token", length = 255)
    private String fcmToken;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ── Constructors ──
    public NotificationSettingsEntity() {}

    // ── Builder ──
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final NotificationSettingsEntity settings = new NotificationSettingsEntity();

        public Builder user(UserEntity user) {
            settings.user = user;
            return this;
        }

        public Builder fcmToken(String fcmToken) {
            settings.fcmToken = fcmToken;
            return this;
        }

        public Builder dailyReminderTime(LocalTime time) {
            settings.dailyReminderTime = time;
            return this;
        }

        public NotificationSettingsEntity build() {
            return settings;
        }
    }

    // ── Getters & Setters ──
    public UUID getId() { return id; }
    public UserEntity getUser() { return user; }
    public void setUser(UserEntity user) { this.user = user; }
    public Boolean getDailyReminderEnabled() { return dailyReminderEnabled; }
    public void setDailyReminderEnabled(Boolean v) { this.dailyReminderEnabled = v; }
    public LocalTime getDailyReminderTime() { return dailyReminderTime; }
    public void setDailyReminderTime(LocalTime t) { this.dailyReminderTime = t; }
    public Boolean getStreakAlertsEnabled() { return streakAlertsEnabled; }
    public void setStreakAlertsEnabled(Boolean v) { this.streakAlertsEnabled = v; }
    public Boolean getGoalAlertsEnabled() { return goalAlertsEnabled; }
    public void setGoalAlertsEnabled(Boolean v) { this.goalAlertsEnabled = v; }
    public String getFcmToken() { return fcmToken; }
    public void setFcmToken(String fcmToken) { this.fcmToken = fcmToken; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}