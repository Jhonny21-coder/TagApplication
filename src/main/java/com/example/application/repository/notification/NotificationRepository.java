package com.example.application.repository.notification;

import com.example.application.data.notification.Notification;
import com.example.application.data.notification.NotificationType;
import com.example.application.data.User;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserId(Long userId);

    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user.id = :userId AND n.isRead = false")
    int markAllAsReadByUserId(Long userId);

    @Modifying
    @Transactional
    @Query("DELETE Notification n WHERE n.referenceId = :referenceId AND n.type = :type")
    int deleteByReferenceIdAndType(@Param("referenceId") Long referenceId, @Param("type") NotificationType type);

    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId AND n.isRead = false")
    List<Notification> findByUserIdAndNotIsRead(@Param("userId") Long userId);
}
