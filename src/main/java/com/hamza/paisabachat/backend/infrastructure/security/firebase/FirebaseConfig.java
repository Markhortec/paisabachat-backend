package com.hamza.paisabachat.backend.infrastructure.security.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${firebase.credentials-path}")
    private String credentialsPath;

    @Value("${firebase.project-id}")
    private String projectId;

    @PostConstruct
    public void initialize() {
        if (FirebaseApp.getApps().isEmpty()) {
            try {
                InputStream serviceAccount = getCredentialsStream();
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .setProjectId(projectId)
                        .build();
                FirebaseApp.initializeApp(options);
                log.info("Firebase initialized successfully for project: {}", projectId);
            } catch (IOException e) {
                log.warn("Firebase credentials not found — " +
                        "Firebase auth will be unavailable: {}", e.getMessage());
            }
        }
    }

    private InputStream getCredentialsStream() throws IOException {
        try {
            return new FileInputStream(credentialsPath);
        } catch (IOException e) {
            InputStream classPathStream = getClass()
                    .getClassLoader()
                    .getResourceAsStream(credentialsPath);
            if (classPathStream != null) {
                return classPathStream;
            }
            throw e;
        }
    }
}