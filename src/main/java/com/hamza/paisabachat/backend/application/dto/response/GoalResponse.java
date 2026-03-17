package com.hamza.paisabachat.backend.application.dto.response;

import com.hamza.paisabachat.backend.infrastructure.persistence.entity.GoalEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class GoalResponse {

    private UUID id;
    private String title;
    private String description;
    private BigDecimal targetAmount;
    private BigDecimal savedAmount;
    private BigDecimal remainingAmount;
    private double progressPercentage;
    private LocalDate deadline;
    private long daysLeft;
    private String priority;
    private String iconName;
    private String status;
    private BigDecimal dailyRequiredAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ── Static Factory ──
    public static GoalResponse fromEntity(GoalEntity entity) {
        GoalResponse response = new GoalResponse();
        response.id = entity.getId();
        response.title = entity.getTitle();
        response.description = entity.getDescription();
        response.targetAmount = entity.getTargetAmount();
        response.savedAmount = entity.getSavedAmount();
        response.remainingAmount = entity.getRemainingAmount();
        response.progressPercentage = entity.getProgressPercentage();
        response.deadline = entity.getDeadline();
        response.priority = entity.getPriority();
        response.iconName = entity.getIconName();
        response.status = entity.getStatus();
        response.createdAt = entity.getCreatedAt();
        response.updatedAt = entity.getUpdatedAt();

        // Calculate days left
        response.daysLeft = ChronoUnit.DAYS.between(
                LocalDate.now(), entity.getDeadline());

        // Calculate daily required amount
        if (response.daysLeft > 0
                && entity.getRemainingAmount()
                .compareTo(BigDecimal.ZERO) > 0) {
            response.dailyRequiredAmount = entity.getRemainingAmount()
                    .divide(BigDecimal.valueOf(response.daysLeft), 2,
                            java.math.RoundingMode.CEILING);
        } else {
            response.dailyRequiredAmount = BigDecimal.ZERO;
        }

        return response;
    }

    // ── Getters ──
    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public BigDecimal getTargetAmount() { return targetAmount; }
    public BigDecimal getSavedAmount() { return savedAmount; }
    public BigDecimal getRemainingAmount() { return remainingAmount; }
    public double getProgressPercentage() { return progressPercentage; }
    public LocalDate getDeadline() { return deadline; }
    public long getDaysLeft() { return daysLeft; }
    public String getPriority() { return priority; }
    public String getIconName() { return iconName; }
    public String getStatus() { return status; }
    public BigDecimal getDailyRequiredAmount() { return dailyRequiredAmount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}