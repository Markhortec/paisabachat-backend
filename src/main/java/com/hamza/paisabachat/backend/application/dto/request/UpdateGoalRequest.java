package com.hamza.paisabachat.backend.application.dto.request;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public class UpdateGoalRequest {

    @Size(min = 2, max = 50, message = "Title must be between 2 and 50 characters")
    @Pattern(
            regexp = "^[a-zA-Z0-9\\s\\u0600-\\u06FF.,!?()-]+$",
            message = "Title contains invalid characters"
    )
    private String title;

    @Size(max = 200, message = "Description cannot exceed 200 characters")
    private String description;

    @Future(message = "Deadline must be a future date")
    private LocalDate deadline;

    @Pattern(
            regexp = "^(LOW|MEDIUM|HIGH)$",
            message = "Priority must be LOW, MEDIUM or HIGH"
    )
    private String priority;

    @Size(max = 50, message = "Icon name too long")
    private String iconName;

    // ── Getters & Setters ──
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public String getIconName() { return iconName; }
    public void setIconName(String iconName) { this.iconName = iconName; }
}