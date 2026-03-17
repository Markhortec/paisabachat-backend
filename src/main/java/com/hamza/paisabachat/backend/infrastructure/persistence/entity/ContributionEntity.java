package com.hamza.paisabachat.backend.infrastructure.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "contributions", indexes = {
        @Index(name = "idx_contributions_goal_id", columnList = "goal_id"),
        @Index(name = "idx_contributions_user_id", columnList = "user_id"),
        @Index(name = "idx_contributions_date", columnList = "contributed_at")
})
public class ContributionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id", nullable = false)
    private GoalEntity goal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(length = 200)
    private String note;

    @Column(name = "contributed_at", nullable = false)
    private LocalDate contributedAt = LocalDate.now();

    @Column(name = "is_synced", nullable = false)
    private Boolean isSynced = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ── Constructors ──
    public ContributionEntity() {}

    // ── Builder ──
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final ContributionEntity contribution = new ContributionEntity();

        public Builder goal(GoalEntity goal) {
            contribution.goal = goal;
            return this;
        }

        public Builder user(UserEntity user) {
            contribution.user = user;
            return this;
        }

        public Builder amount(BigDecimal amount) {
            contribution.amount = amount;
            return this;
        }

        public Builder note(String note) {
            contribution.note = note;
            return this;
        }

        public Builder contributedAt(LocalDate contributedAt) {
            contribution.contributedAt = contributedAt;
            return this;
        }

        public ContributionEntity build() {
            return contribution;
        }
    }

    // ── Getters & Setters ──
    public UUID getId() { return id; }
    public GoalEntity getGoal() { return goal; }
    public void setGoal(GoalEntity goal) { this.goal = goal; }
    public UserEntity getUser() { return user; }
    public void setUser(UserEntity user) { this.user = user; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public LocalDate getContributedAt() { return contributedAt; }
    public void setContributedAt(LocalDate contributedAt) { this.contributedAt = contributedAt; }
    public Boolean getIsSynced() { return isSynced; }
    public void setIsSynced(Boolean isSynced) { this.isSynced = isSynced; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}