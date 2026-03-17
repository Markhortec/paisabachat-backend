package com.hamza.paisabachat.backend.application.dto.response;

import com.hamza.paisabachat.backend.infrastructure.persistence.entity.UserEntity;

import java.time.LocalDateTime;
import java.util.UUID;

public class UserResponse {

    private UUID id;
    private String name;
    private String email;
    private String role;
    private String tier;
    private String avatarUrl;
    private Integer streakCurrent;
    private Integer streakLongest;
    private Long xpTotal;
    private Integer level;
    private Boolean isEmailVerified;
    private LocalDateTime createdAt;

    // ── Static Factory — converts Entity to Response ──
    public static UserResponse fromEntity(UserEntity entity) {
        UserResponse response = new UserResponse();
        response.id = entity.getId();
        response.name = entity.getName();
        response.email = entity.getEmail();
        response.role = entity.getRole();
        response.tier = entity.getTier();
        response.avatarUrl = entity.getAvatarUrl();
        response.streakCurrent = entity.getStreakCurrent();
        response.streakLongest = entity.getStreakLongest();
        response.xpTotal = entity.getXpTotal();
        response.level = entity.getLevel();
        response.isEmailVerified = entity.getIsEmailVerified();
        response.createdAt = entity.getCreatedAt();
        return response;
    }

    // ── Getters ──
    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getTier() { return tier; }
    public String getAvatarUrl() { return avatarUrl; }
    public Integer getStreakCurrent() { return streakCurrent; }
    public Integer getStreakLongest() { return streakLongest; }
    public Long getXpTotal() { return xpTotal; }
    public Integer getLevel() { return level; }
    public Boolean getIsEmailVerified() { return isEmailVerified; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}