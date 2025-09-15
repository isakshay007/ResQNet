package com.resqnet.repository;

import com.resqnet.model.Notification;
import com.resqnet.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // === User-specific ===

    // Fetch all notifications for a specific user
    List<Notification> findByRecipientOrderByCreatedAtDesc(User recipient);

    // Fetch unread notifications for a user
    List<Notification> findByRecipientAndReadFalseOrderByCreatedAtDesc(User recipient);

    // === Admin-wide ===

    // Fetch all admin broadcast notifications
    List<Notification> findByAdminBroadcastTrueOrderByCreatedAtDesc();

    // Fetch unread admin broadcast notifications
    List<Notification> findByAdminBroadcastTrueAndReadFalseOrderByCreatedAtDesc();
}
