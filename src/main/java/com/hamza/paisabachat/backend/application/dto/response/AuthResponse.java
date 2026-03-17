package com.hamza.paisabachat.backend.application.dto.response;

public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private long expiresIn;
    private UserResponse user;

    // ── Builder ──
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final AuthResponse response = new AuthResponse();

        public Builder accessToken(String accessToken) {
            response.accessToken = accessToken;
            return this;
        }

        public Builder refreshToken(String refreshToken) {
            response.refreshToken = refreshToken;
            return this;
        }

        public Builder expiresIn(long expiresIn) {
            response.expiresIn = expiresIn;
            return this;
        }

        public Builder user(UserResponse user) {
            response.user = user;
            return this;
        }

        public AuthResponse build() {
            return response;
        }
    }

    // ── Getters ──
    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public String getTokenType() { return tokenType; }
    public long getExpiresIn() { return expiresIn; }
    public UserResponse getUser() { return user; }
}