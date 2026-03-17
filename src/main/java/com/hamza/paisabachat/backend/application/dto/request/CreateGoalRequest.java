package com.hamza.paisabachat.backend.application.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CreateGoalRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 2, max = 50, message = "Title must be between 2 and 50 characters")
    @Pattern(
            regexp = "^[a-zA-Z0-9\\s\\u0600-\\u06FF.,!?()-]+$",
            message = "Title contains invalid characters"
    )
    private String title;

    @Size(max = 200, message = "Description cannot exceed 200 characters")
    private String description;

    @NotNull(message = "Target amount is required")
    @DecimalMin(value = "100.0", message = "Minimum target amount is PKR 100")
    @DecimalMax(value = "10000000.0", message = "Maximum target amount is PKR 10,000,000")
    @Digits(integer = 10, fraction = 2, message = "Invalid amount format")
    private BigDecimal targetAmount;

    @NotNull(message = "Deadline is required")
    @Future(message = "Deadline must be a future date")
    private LocalDate deadline;

    @NotBlank(message = "Priority is required")
    @Pattern(
            regexp = "^(LOW|MEDIUM|HIGH)$",
            message = "Priority must be LOW, MEDIUM or HIGH"
    )
    private String priority = "MEDIUM";

    @NotBlank(message = "Icon name is required")
    @Size(max = 50, message = "Icon name too long")
    private String iconName = "default";

    // ── Getters & Setters ──
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getTargetAmount() { return targetAmount; }
    public void setTargetAmount(BigDecimal targetAmount) { this.targetAmount = targetAmount; }
    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public String getIconName() { return iconName; }
    public void setIconName(String iconName) { this.iconName = iconName; }
}