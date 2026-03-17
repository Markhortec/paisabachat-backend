package com.hamza.paisabachat.backend.infrastructure.persistence.repository;

import com.hamza.paisabachat.backend.infrastructure.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findByFirebaseUid(String firebaseUid);

    boolean existsByEmail(String email);

    boolean existsByFirebaseUid(String firebaseUid);

    @Query("SELECT u FROM UserEntity u WHERE u.email = :email AND u.isActive = true")
    Optional<UserEntity> findActiveUserByEmail(@Param("email") String email);

    @Query("SELECT COUNT(u) FROM UserEntity u WHERE u.tier = :tier")
    long countByTier(@Param("tier") String tier);
}