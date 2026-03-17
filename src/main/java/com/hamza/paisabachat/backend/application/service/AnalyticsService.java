package com.hamza.paisabachat.backend.application.service;

import com.hamza.paisabachat.backend.application.dto.response.AnalyticsResponse;
import com.hamza.paisabachat.backend.infrastructure.persistence.entity.ContributionEntity;
import com.hamza.paisabachat.backend.infrastructure.persistence.entity.GoalEntity;
import com.hamza.paisabachat.backend.infrastructure.persistence.repository.ContributionRepository;
import com.hamza.paisabachat.backend.infrastructure.persistence.repository.GoalRepository;
import com.hamza.paisabachat.backend.infrastructure.persistence.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    private static final Logger log =
            LoggerFactory.getLogger(AnalyticsService.class);

    private final ContributionRepository contributionRepository;
    private final GoalRepository goalRepository;
    private final UserRepository userRepository;
    private final StreakService streakService;

    public AnalyticsService(
            ContributionRepository contributionRepository,
            GoalRepository goalRepository,
            UserRepository userRepository,
            StreakService streakService) {
        this.contributionRepository = contributionRepository;
        this.goalRepository = goalRepository;
        this.userRepository = userRepository;
        this.streakService = streakService;
    }

    // ── Get Full Analytics ──
    public AnalyticsResponse getAnalytics(String userId) {
        UUID userUUID = UUID.fromString(userId);

        List<ContributionEntity> allContributions =
                contributionRepository.findByUserIdOrderByContributedAtDesc(userUUID);

        List<GoalEntity> allGoals =
                goalRepository.findByUserIdOrderByCreatedAtDesc(userUUID);

        // ── Basic Stats ──
        BigDecimal totalSaved = allContributions.stream()
                .map(ContributionEntity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalTarget = allGoals.stream()
                .map(GoalEntity::getTargetAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        double overallProgress = totalTarget.compareTo(BigDecimal.ZERO) > 0
                ? totalSaved.divide(totalTarget, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)).doubleValue()
                : 0.0;

        BigDecimal averageContribution = allContributions.isEmpty()
                ? BigDecimal.ZERO
                : totalSaved.divide(
                BigDecimal.valueOf(allContributions.size()),
                2, RoundingMode.HALF_UP);

        BigDecimal largestContribution = allContributions.stream()
                .map(ContributionEntity::getAmount)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        long activeGoals = allGoals.stream()
                .filter(g -> "ACTIVE".equals(g.getStatus()))
                .count();

        long completedGoals = allGoals.stream()
                .filter(g -> "COMPLETED".equals(g.getStatus()))
                .count();

        // ── Saving Velocity ──
        String savingVelocity = calculateSavingVelocity(
                allContributions, totalSaved);

        // ── Completion Prediction ──
        String completionPrediction = calculateCompletionPrediction(
                allGoals, allContributions);

        // ── Streak Info ──
        int currentStreak = streakService.getCurrentStreak(userId);
        int longestStreak = streakService.getLongestStreak(userId);
        boolean streakAtRisk = streakService.isStreakAtRisk(userId);

        // ── Daily Data (Last 30 days) ──
        List<AnalyticsResponse.DailyData> dailyData =
                buildDailyData(allContributions);

        // ── Monthly Data (Last 12 months) ──
        List<AnalyticsResponse.MonthlyData> monthlyData =
                buildMonthlyData(allContributions);

        // ── Goal Progress Map ──
        Map<String, BigDecimal> goalProgress = allGoals.stream()
                .collect(Collectors.toMap(
                        GoalEntity::getTitle,
                        g -> BigDecimal.valueOf(g.getProgressPercentage()),
                        (a, b) -> a,
                        LinkedHashMap::new));

        return AnalyticsResponse.builder()
                .totalSaved(totalSaved)
                .totalTarget(totalTarget)
                .overallProgress(overallProgress)
                .currentStreak(currentStreak)
                .longestStreak(longestStreak)
                .totalContributions(allContributions.size())
                .averageContribution(averageContribution)
                .largestContribution(largestContribution)
                .activeGoals(activeGoals)
                .completedGoals(completedGoals)
                .savingVelocity(savingVelocity)
                .completionPrediction(completionPrediction)
                .streakAtRisk(streakAtRisk)
                .dailyData(dailyData)
                .monthlyData(monthlyData)
                .goalProgress(goalProgress)
                .build();
    }

    // ── Private: Saving Velocity ──
    private String calculateSavingVelocity(
            List<ContributionEntity> contributions,
            BigDecimal totalSaved) {

        if (contributions.isEmpty()) {
            return "No contributions yet";
        }

        // Last 7 days average
        LocalDate sevenDaysAgo = LocalDate.now().minusDays(7);
        BigDecimal last7DaysTotal = contributions.stream()
                .filter(c -> !c.getContributedAt().isBefore(sevenDaysAgo))
                .map(ContributionEntity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal dailyAvg7 = last7DaysTotal.divide(
                BigDecimal.valueOf(7), 2, RoundingMode.HALF_UP);

        // Last 30 days average
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        BigDecimal last30DaysTotal = contributions.stream()
                .filter(c -> !c.getContributedAt().isBefore(thirtyDaysAgo))
                .map(ContributionEntity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal dailyAvg30 = last30DaysTotal.divide(
                BigDecimal.valueOf(30), 2, RoundingMode.HALF_UP);

        return String.format(
                "7-day avg: PKR %.0f/day | 30-day avg: PKR %.0f/day",
                dailyAvg7, dailyAvg30);
    }

    // ── Private: Completion Prediction ──
    private String calculateCompletionPrediction(
            List<GoalEntity> goals,
            List<ContributionEntity> contributions) {

        List<GoalEntity> activeGoals = goals.stream()
                .filter(g -> "ACTIVE".equals(g.getStatus()))
                .collect(Collectors.toList());

        if (activeGoals.isEmpty()) {
            return "No active goals";
        }

        if (contributions.isEmpty()) {
            return "Add contributions to get predictions";
        }

        // Calculate daily average from last 30 days
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        BigDecimal last30Total = contributions.stream()
                .filter(c -> !c.getContributedAt().isBefore(thirtyDaysAgo))
                .map(ContributionEntity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal dailyAvg = last30Total.divide(
                BigDecimal.valueOf(30), 2, RoundingMode.HALF_UP);

        if (dailyAvg.compareTo(BigDecimal.ZERO) == 0) {
            return "Increase daily savings to get predictions";
        }

        // Predict for nearest deadline goal
        GoalEntity nearestGoal = activeGoals.stream()
                .min(Comparator.comparing(GoalEntity::getDeadline))
                .orElse(activeGoals.get(0));

        BigDecimal remaining = nearestGoal.getRemainingAmount();
        if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
            return nearestGoal.getTitle() + " is almost complete!";
        }

        long daysNeeded = remaining.divide(
                dailyAvg, 0, RoundingMode.CEILING).longValue();

        LocalDate predictedDate = LocalDate.now().plusDays(daysNeeded);
        LocalDate deadline = nearestGoal.getDeadline();

        if (predictedDate.isBefore(deadline) ||
                predictedDate.isEqual(deadline)) {
            return String.format(
                    "'%s' on track — complete by %s (%.0f days early)",
                    nearestGoal.getTitle(),
                    predictedDate,
                    (double) deadline.toEpochDay() - predictedDate.toEpochDay());
        } else {
            return String.format(
                    "'%s' needs PKR %.0f/day — currently %.0f/day",
                    nearestGoal.getTitle(),
                    remaining.divide(
                            BigDecimal.valueOf(
                                    Math.max(1,
                                            deadline.toEpochDay() -
                                                    LocalDate.now().toEpochDay())),
                            2, RoundingMode.CEILING),
                    dailyAvg);
        }
    }

    // ── Private: Build Daily Data (last 30 days) ──
    private List<AnalyticsResponse.DailyData> buildDailyData(
            List<ContributionEntity> contributions) {

        Map<LocalDate, BigDecimal> dailyMap = contributions.stream()
                .filter(c -> !c.getContributedAt()
                        .isBefore(LocalDate.now().minusDays(30)))
                .collect(Collectors.groupingBy(
                        ContributionEntity::getContributedAt,
                        Collectors.reducing(BigDecimal.ZERO,
                                ContributionEntity::getAmount,
                                BigDecimal::add)));

        List<AnalyticsResponse.DailyData> result = new ArrayList<>();
        for (int i = 29; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            BigDecimal amount = dailyMap.getOrDefault(date, BigDecimal.ZERO);
            result.add(new AnalyticsResponse.DailyData(
                    date, amount, amount.compareTo(BigDecimal.ZERO) > 0));
        }
        return result;
    }

    // ── Private: Build Monthly Data (last 12 months) ──
    private List<AnalyticsResponse.MonthlyData> buildMonthlyData(
            List<ContributionEntity> contributions) {

        Map<String, List<ContributionEntity>> monthlyMap = contributions
                .stream()
                .filter(c -> c.getContributedAt()
                        .isAfter(LocalDate.now().minusMonths(12)))
                .collect(Collectors.groupingBy(c ->
                        c.getContributedAt().getYear() + "-" +
                                c.getContributedAt().getMonthValue()));

        List<AnalyticsResponse.MonthlyData> result = new ArrayList<>();
        for (int i = 11; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusMonths(i);
            String key = date.getYear() + "-" + date.getMonthValue();
            List<ContributionEntity> monthContribs =
                    monthlyMap.getOrDefault(key, List.of());

            BigDecimal total = monthContribs.stream()
                    .map(ContributionEntity::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            result.add(new AnalyticsResponse.MonthlyData(
                    date.getYear(),
                    date.getMonthValue(),
                    Month.of(date.getMonthValue()).name(),
                    total,
                    monthContribs.size()));
        }
        return result;
    }
}