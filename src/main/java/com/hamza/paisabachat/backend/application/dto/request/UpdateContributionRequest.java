package com.hamza.paisabachat.backend.application.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public class UpdateContributionRequest {

    @DecimalMin(value = "1.0", message = "Minimum contribution is PKR 1")
    @DecimalMax(value = "1000000.0", message = "Maximum contribution is PKR 1,000,000")
    @Digits(integer = 10, fraction = 2, message = "Invalid amount format")
    private BigDecimal amount;

    @Size(max = 200, message = "Note cannot exceed 200 characters")
    private String note;

    @PastOrPresent(message = "Contribution date cannot be in the future")
    private LocalDate contributedAt;

    // ── Getters & Setters ──
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public LocalDate getContributedAt() { return contributedAt; }
    public void setContributedAt(LocalDate contributedAt) {
        this.contributedAt = contributedAt;
    }
}