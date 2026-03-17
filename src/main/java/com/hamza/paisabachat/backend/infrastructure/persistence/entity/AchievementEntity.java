package com.hamza.paisabachat.backend.infrastructure.persistence.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "achievements", indexes = {
        @Index(name = "idx_achievements_user", columnList = "user_id")
},
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_achievements_user_key",
                        columnNames = {"user_id", "achievement_key"}
                )
        })
public class AchievementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "achievement_key", nullable = false, length = 50)
    private String achievementKey;

    @Column(nullable = false)
    private Boolean unlocked = false;

    @Column(name = "unlocked_at")
    private LocalDateTime unlockedAt;

    @Column(nullable = false)
    private Integer progress = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ── Constructors ──
    public AchievementEntity() {}

    // ── Business Methods ──
    public void unlock() {
        this.unlocked = true;
        this.unlockedAt = LocalDateTime.now();
    }

    public void updateProgress(int newProgress) {
        this.progress = newProgress;
    }

    // ── Builder ──
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final AchievementEntity achievement = new AchievementEntity();

        public Builder user(UserEntity user) {
            achievement.user = user;
            return this;
        }

        public Builder achievementKey(String achievementKey) {
            achievement.achievementKey = achievementKey;
            return this;
        }

        public Builder progress(Integer progress) {
            achievement.progress = progress;
            return this;
        }

        public AchievementEntity build() {
            return achievement;
        }
    }

    // ── Getters & Setters ──
    public UUID getId() { return id; }
    public UserEntity getUser() { return user; }
    public void setUser(UserEntity user) { this.user = user; }
    public String getAchievementKey() { return achievementKey; }
    public void setAchievementKey(String key) { this.achievementKey = key; }
    public Boolean getUnlocked() { return unlocked; }
    public void setUnlocked(Boolean unlocked) { this.unlocked = unlocked; }
    public LocalDateTime getUnlockedAt() { return unlockedAt; }
    public void setUnlockedAt(LocalDateTime unlockedAt) { this.unlockedAt = unlockedAt; }
    public Integer getProgress() { return progress; }
    public void setProgress(Integer progress) { this.progress = progress; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}