package com.hamza.paisabachat.backend.infrastructure.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "goals", indexes = {
        @Index(name = "idx_goals_user_id", columnList = "user_id"),
        @Index(name = "idx_goals_status", columnList = "status")
})
public class GoalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false, length = 50)
    private String title;

    @Column(length = 200)
    private String description;

    @Column(name = "target_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal targetAmount;

    @Column(name = "saved_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal savedAmount = BigDecimal.ZERO;

    @Column(nullable = false)
    private LocalDate deadline;

    @Column(nullable = false, length = 10)
    private String priority = "MEDIUM";

    @Column(name = "icon_name", nullable = false, length = 50)
    private String iconName = "default";

    @Column(nullable = false, length = 20)
    private String status = "ACTIVE";

    @Column(name = "is_synced", nullable = false)
    private Boolean isSynced = false;

    @OneToMany(mappedBy = "goal",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<ContributionEntity> contributions = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ── Constructors ──
    public GoalEntity() {}

    // ── Business Methods ──
    public boolean isCompleted() {
        return savedAmount.compareTo(targetAmount) >= 0;
    }

    public boolean isActive() {
        return "ACTIVE".equals(status);
    }

    public BigDecimal getRemainingAmount() {
        return targetAmount.subtract(savedAmount).max(BigDecimal.ZERO);
    }

    public double getProgressPercentage() {
        if (targetAmount.compareTo(BigDecimal.ZERO) == 0) return 0;
        return savedAmount.divide(targetAmount, 4,
                        java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }

    // ── Builder ──
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final GoalEntity goal = new GoalEntity();

        public Builder user(UserEntity user) {
            goal.user = user;
            return this;
        }

        public Builder title(String title) {
            goal.title = title;
            return this;
        }

        public Builder description(String description) {
            goal.description = description;
            return this;
        }

        public Builder targetAmount(BigDecimal targetAmount) {
            goal.targetAmount = targetAmount;
            return this;
        }

        public Builder deadline(LocalDate deadline) {
            goal.deadline = deadline;
            return this;
        }

        public Builder priority(String priority) {
            goal.priority = priority;
            return this;
        }

        public Builder iconName(String iconName) {
            goal.iconName = iconName;
            return this;
        }

        public GoalEntity build() {
            return goal;
        }
    }

    // ── Getters & Setters ──
    public UUID getId() { return id; }
    public UserEntity getUser() { return user; }
    public void setUser(UserEntity user) { this.user = user; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getTargetAmount() { return targetAmount; }
    public void setTargetAmount(BigDecimal targetAmount) { this.targetAmount = targetAmount; }
    public BigDecimal getSavedAmount() { return savedAmount; }
    public void setSavedAmount(BigDecimal savedAmount) { this.savedAmount = savedAmount; }
    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public String getIconName() { return iconName; }
    public void setIconName(String iconName) { this.iconName = iconName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Boolean getIsSynced() { return isSynced; }
    public void setIsSynced(Boolean isSynced) { this.isSynced = isSynced; }
    public List<ContributionEntity> getContributions() { return contributions; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}