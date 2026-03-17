package com.hamza.paisabachat.backend.infrastructure.persistence.repository;

import com.hamza.paisabachat.backend.infrastructure.persistence.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, UUID> {

    Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);

    List<RefreshTokenEntity> findByUserIdAndIsRevokedFalse(UUID userId);

    @Modifying
    @Transactional
    @Query("UPDATE RefreshTokenEntity r SET r.isRevoked = true " +
            "WHERE r.user.id = :userId")
    void revokeAllUserTokens(@Param("userId") UUID userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshTokenEntity r " +
            "WHERE r.expiresAt < :now OR r.isRevoked = true")
    void deleteExpiredAndRevokedTokens(@Param("now") LocalDateTime now);

    long countByUserIdAndIsRevokedFalse(UUID userId);
}