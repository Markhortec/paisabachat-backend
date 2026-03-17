package com.hamza.paisabachat.backend.infrastructure.persistence.repository;

import com.hamza.paisabachat.backend.infrastructure.persistence.entity.AchievementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AchievementRepository extends JpaRepository<AchievementEntity, UUID> {

    List<AchievementEntity> findByUserId(UUID userId);

    List<AchievementEntity> findByUserIdAndUnlocked(UUID userId, Boolean unlocked);

    Optional<AchievementEntity> findByUserIdAndAchievementKey(
            UUID userId, String achievementKey);

    boolean existsByUserIdAndAchievementKey(UUID userId, String achievementKey);

    long countByUserIdAndUnlocked(UUID userId, Boolean unlocked);

    @Query("SELECT a FROM AchievementEntity a WHERE a.user.id = :userId " +
            "AND a.unlocked = true ORDER BY a.unlockedAt DESC")
    List<AchievementEntity> findUnlockedByUserIdOrderByDate(
            @Param("userId") UUID userId);

    @Query("SELECT a FROM AchievementEntity a WHERE a.user.id = :userId " +
            "AND a.achievementKey = :key")
    Optional<AchievementEntity> findByUserAndKey(
            @Param("userId") UUID userId,
            @Param("key") String key);
}