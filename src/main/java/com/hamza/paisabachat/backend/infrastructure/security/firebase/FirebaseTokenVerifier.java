package com.hamza.paisabachat.backend.infrastructure.security.firebase;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class FirebaseTokenVerifier {

    private static final Logger log =
            LoggerFactory.getLogger(FirebaseTokenVerifier.class);

    public Optional<FirebaseToken> verifyToken(String idToken) {
        if (FirebaseApp.getApps().isEmpty()) {
            log.warn("Firebase not initialized — cannot verify token");
            return Optional.empty();
        }
        try {
            FirebaseToken token = FirebaseAuth
                    .getInstance()
                    .verifyIdToken(idToken);
            return Optional.of(token);
        } catch (FirebaseAuthException e) {
            log.warn("Firebase token verification failed: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public boolean isFirebaseAvailable() {
        return !FirebaseApp.getApps().isEmpty();
    }
}