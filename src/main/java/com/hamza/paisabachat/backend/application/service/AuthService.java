package com.hamza.paisabachat.backend.application.service;

import com.google.firebase.auth.FirebaseToken;
import com.hamza.paisabachat.backend.application.dto.request.FirebaseAuthRequest;
import com.hamza.paisabachat.backend.application.dto.request.LoginRequest;
import com.hamza.paisabachat.backend.application.dto.request.RegisterRequest;
import com.hamza.paisabachat.backend.application.dto.request.RefreshTokenRequest;
import com.hamza.paisabachat.backend.application.dto.response.AuthResponse;
import com.hamza.paisabachat.backend.application.dto.response.UserResponse;
import com.hamza.paisabachat.backend.domain.exception.BusinessException;
import com.hamza.paisabachat.backend.domain.exception.ResourceNotFoundException;
import com.hamza.paisabachat.backend.infrastructure.persistence.entity.RefreshTokenEntity;
import com.hamza.paisabachat.backend.infrastructure.persistence.entity.UserEntity;
import com.hamza.paisabachat.backend.infrastructure.persistence.repository.RefreshTokenRepository;
import com.hamza.paisabachat.backend.infrastructure.persistence.repository.UserRepository;
import com.hamza.paisabachat.backend.infrastructure.security.firebase.FirebaseTokenVerifier;
import com.hamza.paisabachat.backend.infrastructure.security.jwt.JwtProperties;
import com.hamza.paisabachat.backend.infrastructure.security.jwt.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final PasswordEncoder passwordEncoder;
    private final FirebaseTokenVerifier firebaseTokenVerifier;

    public AuthService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            JwtService jwtService,
            JwtProperties jwtProperties,
            PasswordEncoder passwordEncoder,
            FirebaseTokenVerifier firebaseTokenVerifier) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
        this.passwordEncoder = passwordEncoder;
        this.firebaseTokenVerifier = firebaseTokenVerifier;
    }

    // ── Register with Email & Password ──
    public AuthResponse registerWithEmail(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(
                    "Email already registered. Please login.",
                    "EMAIL_ALREADY_EXISTS"
            );
        }

        UserEntity user = UserEntity.builder()
                .email(request.getEmail())
                .name(request.getName())
                .role("ROLE_USER")
                .tier("FREE")
                .isEmailVerified(false)
                .build();

        userRepository.save(user);
        log.info("New user registered: {}", user.getEmail());

        return buildAuthResponse(user, null);
    }

    // ── Login with Email & Password ──
    public AuthResponse loginWithEmail(LoginRequest request) {
        UserEntity user = userRepository
                .findActiveUserByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(
                        "Invalid email or password.",
                        "INVALID_CREDENTIALS"
                ));

        if (!user.getIsActive()) {
            throw new BusinessException(
                    "Account is deactivated. Please contact support.",
                    "ACCOUNT_DEACTIVATED"
            );
        }

        log.info("User logged in: {}", user.getEmail());
        return buildAuthResponse(user, null);
    }

    // ── Firebase Auth (Google / Email via Firebase) ──
    public AuthResponse loginWithFirebase(FirebaseAuthRequest request) {
        Optional<FirebaseToken> firebaseTokenOpt =
                firebaseTokenVerifier.verifyToken(request.getFirebaseToken());

        if (firebaseTokenOpt.isEmpty()) {
            throw new BusinessException(
                    "Invalid or expired Firebase token.",
                    "INVALID_FIREBASE_TOKEN"
            );
        }

        FirebaseToken firebaseToken = firebaseTokenOpt.get();
        String firebaseUid = firebaseToken.getUid();
        String email = firebaseToken.getEmail();
        String name = firebaseToken.getName();
        boolean emailVerified = firebaseToken.isEmailVerified();

        UserEntity user = userRepository
                .findByFirebaseUid(firebaseUid)
                .orElseGet(() -> {
                    if (email != null) {
                        return userRepository.findByEmail(email)
                                .map(existing -> {
                                    existing.setFirebaseUid(firebaseUid);
                                    existing.setIsEmailVerified(emailVerified);
                                    return userRepository.save(existing);
                                })
                                .orElseGet(() -> createFirebaseUser(
                                        firebaseUid, email, name, emailVerified));
                    }
                    return createFirebaseUser(firebaseUid, email, name, emailVerified);
                });

        log.info("Firebase auth successful for user: {}", user.getEmail());
        return buildAuthResponse(user, request.getDeviceInfo());
    }

    // ── Refresh Token ──
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String tokenHash = hashToken(request.getRefreshToken());

        RefreshTokenEntity refreshToken = refreshTokenRepository
                .findByTokenHash(tokenHash)
                .orElseThrow(() -> new BusinessException(
                        "Invalid refresh token.",
                        "INVALID_REFRESH_TOKEN"
                ));

        if (!refreshToken.isValid()) {
            throw new BusinessException(
                    "Refresh token expired or revoked. Please login again.",
                    "REFRESH_TOKEN_EXPIRED"
            );
        }

        // Rotate refresh token — invalidate old, issue new
        refreshToken.revoke();
        refreshTokenRepository.save(refreshToken);

        UserEntity user = refreshToken.getUser();
        log.info("Token refreshed for user: {}", user.getEmail());

        return buildAuthResponse(user, null);
    }

    // ── Logout ──
    public void logout(String userId) {
        refreshTokenRepository.revokeAllUserTokens(
                UUID.fromString(userId));
        log.info("User logged out: {}", userId);
    }

    // ── Get Current User ──
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String userId) {
        UserEntity user = userRepository
                .findById(UUID.fromString(userId))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", userId));
        return UserResponse.fromEntity(user);
    }

    // ── Private Helpers ──
    private UserEntity createFirebaseUser(
            String firebaseUid, String email,
            String name, boolean emailVerified) {
        UserEntity newUser = UserEntity.builder()
                .firebaseUid(firebaseUid)
                .email(email)
                .name(name)
                .role("ROLE_USER")
                .tier("FREE")
                .isEmailVerified(emailVerified)
                .build();
        return userRepository.save(newUser);
    }

    private AuthResponse buildAuthResponse(UserEntity user, String deviceInfo) {
        String userId = user.getId().toString();
        String accessToken = jwtService.generateAccessToken(
                userId, user.getEmail(), user.getRole());
        String refreshToken = jwtService.generateRefreshToken(userId);

        saveRefreshToken(user, refreshToken, deviceInfo);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtProperties.getAccessTokenExpiry())
                .user(UserResponse.fromEntity(user))
                .build();
    }

    private void saveRefreshToken(
            UserEntity user, String rawToken, String deviceInfo) {
        RefreshTokenEntity tokenEntity = RefreshTokenEntity.builder()
                .user(user)
                .tokenHash(hashToken(rawToken))
                .deviceInfo(deviceInfo)
                .expiresAt(LocalDateTime.now().plusSeconds(
                        jwtProperties.getRefreshTokenExpiry() / 1000))
                .build();
        refreshTokenRepository.save(tokenEntity);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(
                    token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}