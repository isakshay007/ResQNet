package com.resqnet.service;

import com.resqnet.dto.NotificationDTO;
import com.resqnet.model.Notification;
import com.resqnet.model.User;
import com.resqnet.repository.NotificationRepository;
import com.resqnet.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository,
                               UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    // === Save a new notification ===
    @Transactional
    public NotificationDTO saveNotification(NotificationDTO dto) {
        Notification notification;

        if (dto.isAdminBroadcast()) {
            // Broadcast to admins
            notification = new Notification(
                    dto.getMessage(),
                    dto.getType() != null ? dto.getType() : "SYSTEM",
                    dto.isDeletable()
            );
        } else {
            // User-specific
            User user = userRepository.findByEmail(dto.getRecipientEmail())
                    .orElseThrow(() -> new EntityNotFoundException("User not found for notification"));

            notification = new Notification(
                    dto.getMessage(),
                    dto.getType() != null ? dto.getType() : "SYSTEM",
                    user,
                    dto.isDeletable()
            );
        }

        Notification saved = notificationRepository.save(notification);
        return mapToDTO(saved);
    }

    // === Fetch all notifications (user + admin broadcasts if admin) ===
    public List<NotificationDTO> getNotifications(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        List<Notification> notifications = new ArrayList<>(
                notificationRepository.findByRecipientOrderByCreatedAtDesc(user)
        );

        if (isAdmin(user)) {
            notifications.addAll(notificationRepository.findByAdminBroadcastTrueOrderByCreatedAtDesc());
        }

        return notifications.stream()
                .sorted(Comparator.comparing(Notification::getCreatedAt).reversed())
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // === Fetch only unread notifications (user + admin broadcasts if admin) ===
    public List<NotificationDTO> getUnreadNotifications(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        List<Notification> notifications = new ArrayList<>(
                notificationRepository.findByRecipientAndReadFalseOrderByCreatedAtDesc(user)
        );

        if (isAdmin(user)) {
            notifications.addAll(notificationRepository.findByAdminBroadcastTrueAndReadFalseOrderByCreatedAtDesc());
        }

        return notifications.stream()
                .sorted(Comparator.comparing(Notification::getCreatedAt).reversed())
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // === Mark notification as read ===
    @Transactional
    public void markAsRead(Long id, String userEmail) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Notification not found"));

        // allow recipient OR admin (for broadcasts)
        if (!notification.isAdminBroadcast() &&
                (notification.getRecipient() == null ||
                        !notification.getRecipient().getEmail().equalsIgnoreCase(userEmail))) {
            throw new AccessDeniedException("Not authorized to update this notification");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    // === Delete notification (user or admin override) ===
    @Transactional
    public void deleteNotification(Long id, String userEmail, boolean isAdmin) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Notification not found"));

        // Admin override for broadcasts
        if (isAdmin && notification.isAdminBroadcast()) {
            if (!notification.isDeletable()) {
                throw new IllegalArgumentException("This notification cannot be deleted");
            }
            notificationRepository.delete(notification);
            return;
        }

        // User-specific deletion
        if (!notification.isAdminBroadcast() &&
                (notification.getRecipient() == null ||
                        !notification.getRecipient().getEmail().equalsIgnoreCase(userEmail))) {
            throw new AccessDeniedException("Not authorized to delete this notification");
        }

        if (!notification.isDeletable()) {
            throw new IllegalArgumentException("This notification cannot be deleted");
        }

        notificationRepository.delete(notification);
    }

    // === Admin-only helper ===
    public List<NotificationDTO> getAdminNotifications() {
        return notificationRepository.findByAdminBroadcastTrueOrderByCreatedAtDesc().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // === Mapper ===
    private NotificationDTO mapToDTO(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setMessage(notification.getMessage());
        dto.setType(notification.getType());
        dto.setRead(notification.isRead());
        dto.setDeletable(notification.isDeletable());
        dto.setCreatedAt(notification.getCreatedAt());
        dto.setRecipientEmail(
                notification.getRecipient() != null ? notification.getRecipient().getEmail() : "ADMIN-BROADCAST"
        );
        dto.setAdminBroadcast(notification.isAdminBroadcast());
        return dto;
    }

    // === Utility: Safe check for admin role ===
    private boolean isAdmin(User user) {
        return user != null &&
                user.getRole() == User.Role.ADMIN;
    }
}
