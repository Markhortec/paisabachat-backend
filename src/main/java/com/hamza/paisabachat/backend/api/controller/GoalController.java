package com.hamza.paisabachat.backend.api.controller;

import com.hamza.paisabachat.backend.application.dto.request.CreateGoalRequest;
import com.hamza.paisabachat.backend.application.dto.request.UpdateGoalRequest;
import com.hamza.paisabachat.backend.application.dto.response.GoalResponse;
import com.hamza.paisabachat.backend.application.dto.response.GoalSummaryResponse;
import com.hamza.paisabachat.backend.application.service.GoalService;
import com.hamza.paisabachat.backend.shared.util.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/goals")
public class GoalController {

    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    // ── Create Goal ──
    @PostMapping
    public ResponseEntity<ApiResponse<GoalResponse>> createGoal(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody CreateGoalRequest request) {
        GoalResponse response = goalService.createGoal(userId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Goal created successfully", response));
    }

    // ── Get All Goals ──
    @GetMapping
    public ResponseEntity<ApiResponse<List<GoalResponse>>> getAllGoals(
            @AuthenticationPrincipal String userId) {
        List<GoalResponse> goals = goalService.getAllGoals(userId);
        return ResponseEntity.ok(ApiResponse.success(goals));
    }

    // ── Get Active Goals ──
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<GoalResponse>>> getActiveGoals(
            @AuthenticationPrincipal String userId) {
        List<GoalResponse> goals = goalService.getActiveGoals(userId);
        return ResponseEntity.ok(ApiResponse.success(goals));
    }

    // ── Get Goal Summary ──
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<GoalSummaryResponse>> getGoalSummary(
            @AuthenticationPrincipal String userId) {
        GoalSummaryResponse summary = goalService.getGoalSummary(userId);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    // ── Get Goal By Id ──
    @GetMapping("/{goalId}")
    public ResponseEntity<ApiResponse<GoalResponse>> getGoalById(
            @AuthenticationPrincipal String userId,
            @PathVariable UUID goalId) {
        GoalResponse goal = goalService.getGoalById(userId, goalId);
        return ResponseEntity.ok(ApiResponse.success(goal));
    }

    // ── Update Goal ──
    @PutMapping("/{goalId}")
    public ResponseEntity<ApiResponse<GoalResponse>> updateGoal(
            @AuthenticationPrincipal String userId,
            @PathVariable UUID goalId,
            @Valid @RequestBody UpdateGoalRequest request) {
        GoalResponse goal = goalService.updateGoal(userId, goalId, request);
        return ResponseEntity.ok(
                ApiResponse.success("Goal updated successfully", goal));
    }

    // ── Archive Goal ──
    @PatchMapping("/{goalId}/archive")
    public ResponseEntity<ApiResponse<GoalResponse>> archiveGoal(
            @AuthenticationPrincipal String userId,
            @PathVariable UUID goalId) {
        GoalResponse goal = goalService.archiveGoal(userId, goalId);
        return ResponseEntity.ok(
                ApiResponse.success("Goal archived successfully", goal));
    }

    // ── Delete Goal ──
    @DeleteMapping("/{goalId}")
    public ResponseEntity<ApiResponse<Void>> deleteGoal(
            @AuthenticationPrincipal String userId,
            @PathVariable UUID goalId) {
        goalService.deleteGoal(userId, goalId);
        return ResponseEntity.ok(
                ApiResponse.success("Goal deleted successfully"));
    }
} 