package com.hamza.paisabachat.backend.api.controller;

import com.hamza.paisabachat.backend.application.dto.response.UserResponse;
import com.hamza.paisabachat.backend.application.service.UserProfileService;
import com.hamza.paisabachat.backend.infrastructure.persistence.entity.AchievementEntity;
import com.hamza.paisabachat.backend.shared.util.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserProfileController {

    private final UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    // ── Get Profile ──
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(
            @AuthenticationPrincipal String userId) {
        UserResponse profile = userProfileService.getProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    // ── Update Profile ──
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @AuthenticationPrincipal String userId,
            @RequestBody Map<String, String> request) {
        UserResponse updated = userProfileService.updateProfile(
                userId,
                request.get("name"),
                request.get("avatarUrl"));
        return ResponseEntity.ok(
                ApiResponse.success("Profile updated successfully", updated));
    }

    // ── Get Dashboard Summary ──
    @GetMapping("/me/dashboard")
    public ResponseEntity<ApiResponse<UserProfileService.DashboardSummary>>
    getDashboard(@AuthenticationPrincipal String userId) {
        UserProfileService.DashboardSummary summary =
                userProfileService.getDashboardSummary(userId);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    // ── Get All Achievements ──
    @GetMapping("/me/achievements")
    public ResponseEntity<ApiResponse<List<AchievementEntity>>> getAchievements(
            @AuthenticationPrincipal String userId) {
        List<AchievementEntity> achievements =
                userProfileService.getAchievements(userId);
        return ResponseEntity.ok(ApiResponse.success(achievements));
    }

    // ── Get Unlocked Achievements ──
    @GetMapping("/me/achievements/unlocked")
    public ResponseEntity<ApiResponse<List<AchievementEntity>>>
    getUnlockedAchievements(
            @AuthenticationPrincipal String userId) {
        List<AchievementEntity> achievements =
                userProfileService.getUnlockedAchievements(userId);
        return ResponseEntity.ok(ApiResponse.success(achievements));
    }

    // ── Deactivate Account ──
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> deactivateAccount(
            @AuthenticationPrincipal String userId) {
        userProfileService.deactivateAccount(userId);
        return ResponseEntity.ok(
                ApiResponse.success("Account deactivated successfully"));
    }
}