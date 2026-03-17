package com.hamza.paisabachat.backend.api.controller;

import com.hamza.paisabachat.backend.application.dto.request.FirebaseAuthRequest;
import com.hamza.paisabachat.backend.application.dto.request.LoginRequest;
import com.hamza.paisabachat.backend.application.dto.request.RefreshTokenRequest;
import com.hamza.paisabachat.backend.application.dto.request.RegisterRequest;
import com.hamza.paisabachat.backend.application.dto.response.AuthResponse;
import com.hamza.paisabachat.backend.application.dto.response.UserResponse;
import com.hamza.paisabachat.backend.application.service.AuthService;
import com.hamza.paisabachat.backend.shared.util.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // ── Register with Email ──
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.registerWithEmail(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registration successful", response));
    }

    // ── Login with Email ──
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.loginWithEmail(request);
        return ResponseEntity.ok(
                ApiResponse.success("Login successful", response));
    }

    // ── Firebase Auth (Google Sign-In) ──
    @PostMapping("/firebase")
    public ResponseEntity<ApiResponse<AuthResponse>> firebaseAuth(
            @Valid @RequestBody FirebaseAuthRequest request) {
        AuthResponse response = authService.loginWithFirebase(request);
        return ResponseEntity.ok(
                ApiResponse.success("Authentication successful", response));
    }

    // ── Refresh Token ──
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(
                ApiResponse.success("Token refreshed", response));
    }

    // ── Logout ──
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal String userId) {
        authService.logout(userId);
        return ResponseEntity.ok(
                ApiResponse.success("Logged out successfully"));
    }

    // ── Get Current User ──
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
            @AuthenticationPrincipal String userId) {
        UserResponse user = authService.getCurrentUser(userId);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    // ── Health Check ──
    @GetMapping("/ping")
    public ResponseEntity<ApiResponse<String>> ping() {
        return ResponseEntity.ok(
                ApiResponse.success("PaisaBachat API is running!"));
    }
}