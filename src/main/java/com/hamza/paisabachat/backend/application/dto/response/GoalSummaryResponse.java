package com.hamza.paisabachat.backend.application.dto.response;

import java.math.BigDecimal;
import java.util.List;

public class GoalSummaryResponse {

    private long totalGoals;
    private long activeGoals;
    private long completedGoals;
    private long archivedGoals;
    private BigDecimal totalSaved;
    private BigDecimal totalTarget;
    private double overallProgress;
    private List<GoalResponse> goals;

    // ── Builder ──
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final GoalSummaryResponse summary = new GoalSummaryResponse();

        public Builder totalGoals(long totalGoals) {
            summary.totalGoals = totalGoals;
            return this;
        }

        public Builder activeGoals(long activeGoals) {
            summary.activeGoals = activeGoals;
            return this;
        }

        public Builder completedGoals(long completedGoals) {
            summary.completedGoals = completedGoals;
            return this;
        }

        public Builder archivedGoals(long archivedGoals) {
            summary.archivedGoals = archivedGoals;
            return this;
        }

        public Builder totalSaved(BigDecimal totalSaved) {
            summary.totalSaved = totalSaved;
            return this;
        }

        public Builder totalTarget(BigDecimal totalTarget) {
            summary.totalTarget = totalTarget;
            return this;
        }

        public Builder overallProgress(double overallProgress) {
            summary.overallProgress = overallProgress;
            return this;
        }

        public Builder goals(List<GoalResponse> goals) {
            summary.goals = goals;
            return this;
        }

        public GoalSummaryResponse build() {
            return summary;
        }
    }

    // ── Getters ──
    public long getTotalGoals() { return totalGoals; }
    public long getActiveGoals() { return activeGoals; }
    public long getCompletedGoals() { return completedGoals; }
    public long getArchivedGoals() { return archivedGoals; }
    public BigDecimal getTotalSaved() { return totalSaved; }
    public BigDecimal getTotalTarget() { return totalTarget; }
    public double getOverallProgress() { return overallProgress; }
    public List<GoalResponse> getGoals() { return goals; }
}