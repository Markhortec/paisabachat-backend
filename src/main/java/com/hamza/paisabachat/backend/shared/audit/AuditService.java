package com.hamza.paisabachat.backend.shared.audit;

import com.hamza.paisabachat.backend.infrastructure.persistence.entity.AuditLogEntity;
import com.hamza.paisabachat.backend.infrastructure.persistence.entity.UserEntity;
import com.hamza.paisabachat.backend.infrastructure.persistence.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.UUID;

@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public AuditService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(
            String userId,
            String action,
            String entityType,
            UUID entityId,
            String oldValue,
            String newValue,
            String ipAddress,
            String userAgent) {
        try {
            AuditLogEntity auditLog = AuditLogEntity.builder()
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .oldValue(oldValue)
                    .newValue(newValue)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .build();

            if (userId != null) {
                userRepository.findById(UUID.fromString(userId))
                        .ifPresent(auditLog::setUser);
            }

            entityManager.persist(auditLog);
            log.debug("Audit log saved: {} - {}", action, entityType);
        } catch (Exception e) {
            log.error("Failed to save audit log: {}", e.getMessage());
        }
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logSimple(String userId, String action, String entityType) {
        log(userId, action, entityType, null, null, null, null, null);
    }
}