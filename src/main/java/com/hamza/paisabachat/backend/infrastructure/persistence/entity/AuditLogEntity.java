package com.hamza.paisabachat.backend.infrastructure.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_audit_logs_user_id", columnList = "user_id"),
        @Index(name = "idx_audit_logs_created_at", columnList = "created_at")
})
public class AuditLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(nullable = false, length = 100)
    private String action;

    @Column(name = "entity_type", length = 50)
    private String entityType;

    @Column(name = "entity_id")
    private UUID entityId;

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ── Constructors ──
    public AuditLogEntity() {}

    // ── Builder ──
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final AuditLogEntity log = new AuditLogEntity();

        public Builder user(UserEntity user) {
            log.user = user;
            return this;
        }

        public Builder action(String action) {
            log.action = action;
            return this;
        }

        public Builder entityType(String entityType) {
            log.entityType = entityType;
            return this;
        }

        public Builder entityId(UUID entityId) {
            log.entityId = entityId;
            return this;
        }

        public Builder oldValue(String oldValue) {
            log.oldValue = oldValue;
            return this;
        }

        public Builder newValue(String newValue) {
            log.newValue = newValue;
            return this;
        }

        public Builder ipAddress(String ipAddress) {
            log.ipAddress = ipAddress;
            return this;
        }

        public Builder userAgent(String userAgent) {
            log.userAgent = userAgent;
            return this;
        }

        public AuditLogEntity build() {
            return log;
        }
    }

    // ── Getters & Setters ──
    public UUID getId() { return id; }
    public UserEntity getUser() { return user; }
    public void setUser(UserEntity user) { this.user = user; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    public UUID getEntityId() { return entityId; }
    public void setEntityId(UUID entityId) { this.entityId = entityId; }
    public String getOldValue() { return oldValue; }
    public void setOldValue(String oldValue) { this.oldValue = oldValue; }
    public String getNewValue() { return newValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}