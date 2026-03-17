package com.hamza.paisabachat.backend.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public class FirebaseAuthRequest {

    @NotBlank(message = "Firebase token is required")
    private String firebaseToken;

    private String deviceInfo;
    private String fcmToken;

    // ── Getters & Setters ──
    public String getFirebaseToken() { return firebaseToken; }
    public void setFirebaseToken(String firebaseToken) { this.firebaseToken = firebaseToken; }
    public String getDeviceInfo() { return deviceInfo; }
    public void setDeviceInfo(String deviceInfo) { this.deviceInfo = deviceInfo; }
    public String getFcmToken() { return fcmToken; }
    public void setFcmToken(String fcmToken) { this.fcmToken = fcmToken; }
}