package com.hamza.paisabachat.backend.api.controller;

import com.hamza.paisabachat.backend.application.dto.request.CreateContributionRequest;
import com.hamza.paisabachat.backend.application.dto.request.UpdateContributionRequest;
import com.hamza.paisabachat.backend.application.dto.response.ContributionResponse;
import com.hamza.paisabachat.backend.application.service.ContributionService;
import com.hamza.paisabachat.backend.shared.util.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/contributions")
public class ContributionController {

    private final ContributionService contributionService;

    public ContributionController(ContributionService contributionService) {
        this.contributionService = contributionService;
    }

    // ── Add Contribution ──
    @PostMapping
    public ResponseEntity<ApiResponse<ContributionResponse>> addContribution(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody CreateContributionRequest request) {
        ContributionResponse response =
                contributionService.addContribution(userId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Contribution added successfully", response));
    }

    // ── Get All Contributions ──
    @GetMapping
    public ResponseEntity<ApiResponse<List<ContributionResponse>>> getAllContributions(
            @AuthenticationPrincipal String userId) {
        List<ContributionResponse> contributions =
                contributionService.getAllContributions(userId);
        return ResponseEntity.ok(ApiResponse.success(contributions));
    }

    // ── Get Contributions by Goal ──
    @GetMapping("/goal/{goalId}")
    public ResponseEntity<ApiResponse<List<ContributionResponse>>> getByGoal(
            @AuthenticationPrincipal String userId,
            @PathVariable UUID goalId) {
        List<ContributionResponse> contributions =
                contributionService.getContributionsByGoal(userId, goalId);
        return ResponseEntity.ok(ApiResponse.success(contributions));
    }

    // ── Get Contribution By Id ──
    @GetMapping("/{contributionId}")
    public ResponseEntity<ApiResponse<ContributionResponse>> getById(
            @AuthenticationPrincipal String userId,
            @PathVariable UUID contributionId) {
        ContributionResponse contribution =
                contributionService.getContributionById(userId, contributionId);
        return ResponseEntity.ok(ApiResponse.success(contribution));
    }

    // ── Get By Date Range ──
    @GetMapping("/range")
    public ResponseEntity<ApiResponse<List<ContributionResponse>>> getByDateRange(
            @AuthenticationPrincipal String userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate) {
        List<ContributionResponse> contributions =
                contributionService.getContributionsByDateRange(
                        userId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(contributions));
    }

    // ── Update Contribution ──
    @PutMapping("/{contributionId}")
    public ResponseEntity<ApiResponse<ContributionResponse>> updateContribution(
            @AuthenticationPrincipal String userId,
            @PathVariable UUID contributionId,
            @Valid @RequestBody UpdateContributionRequest request) {
        ContributionResponse contribution =
                contributionService.updateContribution(
                        userId, contributionId, request);
        return ResponseEntity.ok(
                ApiResponse.success("Contribution updated successfully",
                        contribution));
    }

    // ── Delete Contribution ──
    @DeleteMapping("/{contributionId}")
    public ResponseEntity<ApiResponse<Void>> deleteContribution(
            @AuthenticationPrincipal String userId,
            @PathVariable UUID contributionId) {
        contributionService.deleteContribution(userId, contributionId);
        return ResponseEntity.ok(
                ApiResponse.success("Contribution deleted successfully"));
    }
}