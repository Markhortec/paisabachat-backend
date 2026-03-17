package com.hamza.paisabachat.backend.api.controller;

import com.hamza.paisabachat.backend.infrastructure.notification.NotificationService;
import com.hamza.paisabachat.backend.infrastructure.persistence.entity.NotificationSettingsEntity;
import com.hamza.paisabachat.backend.shared.util.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // ── Update FCM Token ──
    @PostMapping("/fcm-token")
    public ResponseEntity<ApiResponse<Void>> updateFcmToken(
            @AuthenticationPrincipal String userId,
            @RequestBody Map<String, String> request) {
        String fcmToken = request.get("fcmToken");
        if (fcmToken == null || fcmToken.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(
                            "FCM token is required",
                            "INVALID_FCM_TOKEN"));
        }
        notificationService.updateFcmToken(userId, fcmToken);
        return ResponseEntity.ok(
                ApiResponse.success("FCM token updated successfully"));
    }

    // ── Get Notification Settings ──
    @GetMapping("/settings")
    public ResponseEntity<ApiResponse<NotificationSettingsEntity>> getSettings(
            @AuthenticationPrincipal String userId) {
        Optional<NotificationSettingsEntity> settings =
                notificationService.getSettings(userId);
        return settings.map(s -> ResponseEntity.ok(ApiResponse.success(s)))
                .orElse(ResponseEntity.ok(
                        ApiResponse.success("No settings found", null)));
    }
}