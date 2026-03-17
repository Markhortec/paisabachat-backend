package com.hamza.paisabachat.backend.application.dto.response;

import com.hamza.paisabachat.backend.infrastructure.persistence.entity.ContributionEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class ContributionResponse {

    private UUID id;
    private UUID goalId;
    private String goalTitle;
    private BigDecimal amount;
    private String note;
    private LocalDate contributedAt;
    private LocalDateTime createdAt;

    // ── Static Factory ──
    public static ContributionResponse fromEntity(ContributionEntity entity) {
        ContributionResponse response = new ContributionResponse();
        response.id = entity.getId();
        response.goalId = entity.getGoal().getId();
        response.goalTitle = entity.getGoal().getTitle();
        response.amount = entity.getAmount();
        response.note = entity.getNote();
        response.contributedAt = entity.getContributedAt();
        response.createdAt = entity.getCreatedAt();
        return response;
    }

    // ── Getters ──
    public UUID getId() { return id; }
    public UUID getGoalId() { return goalId; }
    public String getGoalTitle() { return goalTitle; }
    public BigDecimal getAmount() { return amount; }
    public String getNote() { return note; }
    public LocalDate getContributedAt() { return contributedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}