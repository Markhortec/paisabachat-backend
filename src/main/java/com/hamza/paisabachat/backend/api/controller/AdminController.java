package com.hamza.paisabachat.backend.api.controller;

import com.hamza.paisabachat.backend.application.dto.response.UserResponse;
import com.hamza.paisabachat.backend.application.service.AdminService;
import com.hamza.paisabachat.backend.shared.util.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // ── Get All Users ──
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<UserResponse> users = adminService.getAllUsers(page, size);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    // ── Get User By Id ──
    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(
            @PathVariable String userId) {
        UserResponse user = adminService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    // ── Activate User ──
    @PatchMapping("/users/{userId}/activate")
    public ResponseEntity<ApiResponse<UserResponse>> activateUser(
            @AuthenticationPrincipal String adminId,
            @PathVariable String userId) {
        UserResponse user = adminService.toggleUserStatus(
                adminId, userId, true);
        return ResponseEntity.ok(
                ApiResponse.success("User activated successfully", user));
    }

    // ── Deactivate User ──
    @PatchMapping("/users/{userId}/deactivate")
    public ResponseEntity<ApiResponse<UserResponse>> deactivateUser(
            @AuthenticationPrincipal String adminId,
            @PathVariable String userId) {
        UserResponse user = adminService.toggleUserStatus(
                adminId, userId, false);
        return ResponseEntity.ok(
                ApiResponse.success("User deactivated successfully", user));
    }

    // ── Change User Tier ──
    @PatchMapping("/users/{userId}/tier")
    public ResponseEntity<ApiResponse<UserResponse>> changeUserTier(
            @AuthenticationPrincipal String adminId,
            @PathVariable String userId,
            @RequestParam String tier) {
        UserResponse user = adminService.changeUserTier(
                adminId, userId, tier);
        return ResponseEntity.ok(
                ApiResponse.success("Tier updated successfully", user));
    }

    // ── Platform Stats ──
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPlatformStats() {
        Map<String, Object> stats = adminService.getPlatformStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}