package com.hamza.paisabachat.backend.infrastructure.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_email", columnList = "email"),
        @Index(name = "idx_users_firebase_uid", columnList = "firebase_uid")
})
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "firebase_uid", unique = true, length = 128)
    private String firebaseUid;

    @Column(unique = true, length = 255)
    private String email;

    @Column(length = 100)
    private String name;

    @Column(length = 20)
    private String phone;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(nullable = false, length = 20)
    private String role = "ROLE_USER";

    @Column(nullable = false, length = 20)
    private String tier = "FREE";

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "is_email_verified", nullable = false)
    private Boolean isEmailVerified = false;

    @Column(name = "streak_current", nullable = false)
    private Integer streakCurrent = 0;

    @Column(name = "streak_longest", nullable = false)
    private Integer streakLongest = 0;

    @Column(name = "streak_last_date")
    private LocalDate streakLastDate;

    @Column(name = "xp_total", nullable = false)
    private Long xpTotal = 0L;

    @Column(nullable = false)
    private Integer level = 1;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ── Constructors ──
    public UserEntity() {}

    // ── Builder Pattern ──
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final UserEntity user = new UserEntity();

        public Builder firebaseUid(String firebaseUid) {
            user.firebaseUid = firebaseUid;
            return this;
        }

        public Builder email(String email) {
            user.email = email;
            return this;
        }

        public Builder name(String name) {
            user.name = name;
            return this;
        }

        public Builder phone(String phone) {
            user.phone = phone;
            return this;
        }

        public Builder role(String role) {
            user.role = role;
            return this;
        }

        public Builder tier(String tier) {
            user.tier = tier;
            return this;
        }

        public Builder isEmailVerified(Boolean isEmailVerified) {
            user.isEmailVerified = isEmailVerified;
            return this;
        }

        public UserEntity build() {
            return user;
        }
    }

    // ── Getters & Setters ──
    public UUID getId() { return id; }
    public String getFirebaseUid() { return firebaseUid; }
    public void setFirebaseUid(String firebaseUid) { this.firebaseUid = firebaseUid; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getTier() { return tier; }
    public void setTier(String tier) { this.tier = tier; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public Boolean getIsEmailVerified() { return isEmailVerified; }
    public void setIsEmailVerified(Boolean isEmailVerified) { this.isEmailVerified = isEmailVerified; }
    public Integer getStreakCurrent() { return streakCurrent; }
    public void setStreakCurrent(Integer streakCurrent) { this.streakCurrent = streakCurrent; }
    public Integer getStreakLongest() { return streakLongest; }
    public void setStreakLongest(Integer streakLongest) { this.streakLongest = streakLongest; }
    public LocalDate getStreakLastDate() { return streakLastDate; }
    public void setStreakLastDate(LocalDate streakLastDate) { this.streakLastDate = streakLastDate; }
    public Long getXpTotal() { return xpTotal; }
    public void setXpTotal(Long xpTotal) { this.xpTotal = xpTotal; }
    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}