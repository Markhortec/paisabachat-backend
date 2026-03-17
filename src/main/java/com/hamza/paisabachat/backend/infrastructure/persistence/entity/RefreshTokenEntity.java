package com.hamza.paisabachat.backend.infrastructure.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens", indexes = {
        @Index(name = "idx_refresh_tokens_user", columnList = "user_id")
})
public class RefreshTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "token_hash", nullable = false, unique = true, length = 255)
    private String tokenHash;

    @Column(name = "device_info", length = 255)
    private String deviceInfo;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "is_revoked", nullable = false)
    private Boolean isRevoked = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ── Constructors ──
    public RefreshTokenEntity() {}

    // ── Business Methods ──
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !isRevoked && !isExpired();
    }

    public void revoke() {
        this.isRevoked = true;
    }

    // ── Builder ──
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final RefreshTokenEntity token = new RefreshTokenEntity();

        public Builder user(UserEntity user) {
            token.user = user;
            return this;
        }

        public Builder tokenHash(String tokenHash) {
            token.tokenHash = tokenHash;
            return this;
        }

        public Builder deviceInfo(String deviceInfo) {
            token.deviceInfo = deviceInfo;
            return this;
        }

        public Builder ipAddress(String ipAddress) {
            token.ipAddress = ipAddress;
            return this;
        }

        public Builder expiresAt(LocalDateTime expiresAt) {
            token.expiresAt = expiresAt;
            return this;
        }

        public RefreshTokenEntity build() {
            return token;
        }
    }

    // ── Getters & Setters ──
    public UUID getId() { return id; }
    public UserEntity getUser() { return user; }
    public void setUser(UserEntity user) { this.user = user; }
    public String getTokenHash() { return tokenHash; }
    public void setTokenHash(String tokenHash) { this.tokenHash = tokenHash; }
    public String getDeviceInfo() { return deviceInfo; }
    public void setDeviceInfo(String deviceInfo) { this.deviceInfo = deviceInfo; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public Boolean getIsRevoked() { return isRevoked; }
    public void setIsRevoked(Boolean isRevoked) { this.isRevoked = isRevoked; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}