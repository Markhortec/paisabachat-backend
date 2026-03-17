package com.hamza.paisabachat.backend.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class AnalyticsResponse {

    private BigDecimal totalSaved;
    private BigDecimal totalTarget;
    private double overallProgress;
    private int currentStreak;
    private int longestStreak;
    private long totalContributions;
    private BigDecimal averageContribution;
    private BigDecimal largestContribution;
    private long activeGoals;
    private long completedGoals;
    private String savingVelocity;
    private String completionPrediction;
    private boolean streakAtRisk;
    private List<DailyData> dailyData;
    private List<MonthlyData> monthlyData;
    private Map<String, BigDecimal> goalProgress;

    // ── Nested: Daily Data ──
    public static class DailyData {
        private LocalDate date;
        private BigDecimal amount;
        private boolean contributed;

        public DailyData(LocalDate date, BigDecimal amount, boolean contributed) {
            this.date = date;
            this.amount = amount;
            this.contributed = contributed;
        }

        public LocalDate getDate() { return date; }
        public BigDecimal getAmount() { return amount; }
        public boolean isContributed() { return contributed; }
    }

    // ── Nested: Monthly Data ──
    public static class MonthlyData {
        private int year;
        private int month;
        private String monthName;
        private BigDecimal totalAmount;
        private long contributionCount;

        public MonthlyData(int year, int month, String monthName,
                           BigDecimal totalAmount, long contributionCount) {
            this.year = year;
            this.month = month;
            this.monthName = monthName;
            this.totalAmount = totalAmount;
            this.contributionCount = contributionCount;
        }

        public int getYear() { return year; }
        public int getMonth() { return month; }
        public String getMonthName() { return monthName; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public long getContributionCount() { return contributionCount; }
    }

    // ── Builder ──
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final AnalyticsResponse r = new AnalyticsResponse();

        public Builder totalSaved(BigDecimal v) { r.totalSaved = v; return this; }
        public Builder totalTarget(BigDecimal v) { r.totalTarget = v; return this; }
        public Builder overallProgress(double v) { r.overallProgress = v; return this; }
        public Builder currentStreak(int v) { r.currentStreak = v; return this; }
        public Builder longestStreak(int v) { r.longestStreak = v; return this; }
        public Builder totalContributions(long v) { r.totalContributions = v; return this; }
        public Builder averageContribution(BigDecimal v) { r.averageContribution = v; return this; }
        public Builder largestContribution(BigDecimal v) { r.largestContribution = v; return this; }
        public Builder activeGoals(long v) { r.activeGoals = v; return this; }
        public Builder completedGoals(long v) { r.completedGoals = v; return this; }
        public Builder savingVelocity(String v) { r.savingVelocity = v; return this; }
        public Builder completionPrediction(String v) { r.completionPrediction = v; return this; }
        public Builder streakAtRisk(boolean v) { r.streakAtRisk = v; return this; }
        public Builder dailyData(List<DailyData> v) { r.dailyData = v; return this; }
        public Builder monthlyData(List<MonthlyData> v) { r.monthlyData = v; return this; }
        public Builder goalProgress(Map<String, BigDecimal> v) { r.goalProgress = v; return this; }
        public AnalyticsResponse build() { return r; }
    }

    // ── Getters ──
    public BigDecimal getTotalSaved() { return totalSaved; }
    public BigDecimal getTotalTarget() { return totalTarget; }
    public double getOverallProgress() { return overallProgress; }
    public int getCurrentStreak() { return currentStreak; }
    public int getLongestStreak() { return longestStreak; }
    public long getTotalContributions() { return totalContributions; }
    public BigDecimal getAverageContribution() { return averageContribution; }
    public BigDecimal getLargestContribution() { return largestContribution; }
    public long getActiveGoals() { return activeGoals; }
    public long getCompletedGoals() { return completedGoals; }
    public String getSavingVelocity() { return savingVelocity; }
    public String getCompletionPrediction() { return completionPrediction; }
    public boolean isStreakAtRisk() { return streakAtRisk; }
    public List<DailyData> getDailyData() { return dailyData; }
    public List<MonthlyData> getMonthlyData() { return monthlyData; }
    public Map<String, BigDecimal> getGoalProgress() { return goalProgress; }
}