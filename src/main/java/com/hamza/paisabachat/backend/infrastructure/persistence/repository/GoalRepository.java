package com.hamza.paisabachat.backend.infrastructure.persistence.repository;

import com.hamza.paisabachat.backend.infrastructure.persistence.entity.GoalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GoalRepository extends JpaRepository<GoalEntity, UUID> {

    List<GoalEntity> findByUserIdAndStatusOrderByCreatedAtDesc(
            UUID userId, String status);

    List<GoalEntity> findByUserIdOrderByCreatedAtDesc(UUID userId);

    Optional<GoalEntity> findByIdAndUserId(UUID id, UUID userId);

    long countByUserIdAndStatus(UUID userId, String status);

    boolean existsByIdAndUserId(UUID id, UUID userId);

    @Query("SELECT g FROM GoalEntity g WHERE g.user.id = :userId " +
            "AND g.status = 'ACTIVE' ORDER BY g.deadline ASC")
    List<GoalEntity> findActiveGoalsByUserIdOrderByDeadline(
            @Param("userId") UUID userId);

    @Query("SELECT COUNT(g) FROM GoalEntity g WHERE g.user.id = :userId " +
            "AND g.status = 'ACTIVE'")
    long countActiveGoalsByUserId(@Param("userId") UUID userId);

    @Query("SELECT g FROM GoalEntity g WHERE g.user.id = :userId " +
            "AND g.status = 'ACTIVE' AND g.deadline < CURRENT_DATE")
    List<GoalEntity> findOverdueGoals(@Param("userId") UUID userId);
}