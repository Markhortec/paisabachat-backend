package com.hamza.paisabachat.backend.infrastructure.persistence.repository;

import com.hamza.paisabachat.backend.infrastructure.persistence.entity.NotificationSettingsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationSettingsRepository
        extends JpaRepository<NotificationSettingsEntity, UUID> {

    Optional<NotificationSettingsEntity> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);

    @Query("SELECT n FROM NotificationSettingsEntity n " +
            "WHERE n.dailyReminderEnabled = true " +
            "AND n.dailyReminderTime = :time")
    List<NotificationSettingsEntity> findUsersForReminder(
            @Param("time") LocalTime time);

    @Query("SELECT n FROM NotificationSettingsEntity n " +
            "WHERE n.streakAlertsEnabled = true " +
            "AND n.fcmToken IS NOT NULL")
    List<NotificationSettingsEntity> findUsersWithStreakAlerts();
}