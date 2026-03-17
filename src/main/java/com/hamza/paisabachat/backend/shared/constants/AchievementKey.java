package com.hamza.paisabachat.backend.shared.constants;

public enum AchievementKey {

    // ── Savings Milestones ──
    FIRST_STEP("First Step", "Save PKR 1,000 total", 1000),
    RISING_SAVER("Rising Saver", "Save PKR 10,000 total", 10000),
    WEALTH_BUILDER("Wealth Builder", "Save PKR 100,000 total", 100000),
    SAVINGS_KING("Savings King", "Save PKR 1,000,000 total", 1000000),

    // ── Consistency ──
    WEEK_WARRIOR("Week Warrior", "7-day saving streak", 7),
    MONTH_MASTER("Month Master", "30-day saving streak", 30),
    CENTURY_CLUB("Century Club", "100-day saving streak", 100),

    // ── Goals ──
    GOAL_GETTER("Goal Getter", "Complete your first goal", 1),
    MULTI_TASKER("Multi Tasker", "Complete 5 goals", 5),
    DREAM_CHASER("Dream Chaser", "Complete 10 goals", 10),

    // ── First Actions ──
    FIRST_CONTRIBUTION("First Contribution", "Make your first contribution", 1),
    FIRST_GOAL("First Goal", "Create your first goal", 1),

    // ── Special ──
    EARLY_BIRD("Early Bird", "Save before 8 AM", 1),
    CONSISTENCY_MASTER("Consistency Master", "Save every day for a month", 30);

    private final String displayName;
    private final String description;
    private final int targetValue;

    AchievementKey(String displayName, String description, int targetValue) {
        this.displayName = displayName;
        this.description = description;
        this.targetValue = targetValue;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public int getTargetValue() { return targetValue; }
}