package com.hamza.paisabachat.backend.api.controller;

import com.hamza.paisabachat.backend.application.dto.response.AnalyticsResponse;
import com.hamza.paisabachat.backend.application.service.AnalyticsService;
import com.hamza.paisabachat.backend.shared.util.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    // ── Get Full Analytics ──
    @GetMapping
    public ResponseEntity<ApiResponse<AnalyticsResponse>> getAnalytics(
            @AuthenticationPrincipal String userId) {
        AnalyticsResponse analytics = analyticsService.getAnalytics(userId);
        return ResponseEntity.ok(ApiResponse.success(analytics));
    }
}