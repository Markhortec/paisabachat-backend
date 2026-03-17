package com.hamza.paisabachat.backend.infrastructure.persistence.repository;

import com.hamza.paisabachat.backend.infrastructure.persistence.entity.ContributionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContributionRepository extends JpaRepository<ContributionEntity, UUID> {

    List<ContributionEntity> findByGoalIdOrderByContributedAtDesc(UUID goalId);

    List<ContributionEntity> findByUserIdOrderByContributedAtDesc(UUID userId);

    Optional<ContributionEntity> findByIdAndUserId(UUID id, UUID userId);

    boolean existsByIdAndUserId(UUID id, UUID userId);

    @Query("SELECT SUM(c.amount) FROM ContributionEntity c " +
            "WHERE c.goal.id = :goalId")
    Optional<BigDecimal> sumAmountByGoalId(@Param("goalId") UUID goalId);

    @Query("SELECT SUM(c.amount) FROM ContributionEntity c " +
            "WHERE c.user.id = :userId")
    Optional<BigDecimal> sumAmountByUserId(@Param("userId") UUID userId);

    @Query("SELECT c FROM ContributionEntity c WHERE c.user.id = :userId " +
            "AND c.contributedAt BETWEEN :startDate AND :endDate " +
            "ORDER BY c.contributedAt DESC")
    List<ContributionEntity> findByUserIdAndDateRange(
            @Param("userId") UUID userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(c) > 0 FROM ContributionEntity c " +
            "WHERE c.user.id = :userId AND c.contributedAt = :date")
    boolean existsByUserIdAndDate(
            @Param("userId") UUID userId,
            @Param("date") LocalDate date);

    @Query("SELECT AVG(c.amount) FROM ContributionEntity c " +
            "WHERE c.user.id = :userId")
    Optional<BigDecimal> findAverageAmountByUserId(@Param("userId") UUID userId);

    @Query("SELECT c FROM ContributionEntity c WHERE c.user.id = :userId " +
            "ORDER BY c.contributedAt DESC")
    List<ContributionEntity> findRecentByUserId(
            @Param("userId") UUID userId,
            org.springframework.data.domain.Pageable pageable);
    @Query("SELECT COUNT(c) FROM ContributionEntity c WHERE c.user.id = :userId")
    long countByUserIdAndDate(@Param("userId") UUID userId);
}