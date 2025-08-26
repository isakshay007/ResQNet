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

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    //  Save a new notification (skip if user not found to avoid Kafka retry loop)
    @Transactional
    public NotificationDTO saveNotification(NotificationDTO dto) {
        return userRepository.findByEmail(dto.getRecipientEmail())
                .map(user -> {
                    Notification notification = new Notification(
                            dto.getMessage(),
                            dto.getType() != null ? dto.getType() : "SYSTEM",
                            user,
                            dto.isDeletable()
                    );
                    Notification saved = notificationRepository.save(notification);
                    return mapToDTO(saved);
                })
                .orElseGet(() -> {
                    log.warn("Skipping notification for missing user: {}", dto.getRecipientEmail());
                    return null; // message dropped gracefully
                });
    }

    // Fetch all notifications for a user
    public List<NotificationDTO> getNotifications(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return notificationRepository.findByRecipientOrderByCreatedAtDesc(user).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    //  Fetch only unread notifications
    public List<NotificationDTO> getUnreadNotifications(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return notificationRepository.findByRecipientAndReadFalseOrderByCreatedAtDesc(user).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    //  Mark notification as read (403 if not owner)
    @Transactional
    public void markAsRead(Long id, String userEmail) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Notification not found"));

        if (!notification.getRecipient().getEmail().equalsIgnoreCase(userEmail)) {
            throw new AccessDeniedException("Not authorized to update this notification");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    //  Delete notification (403 if not owner, 400 if non-deletable)
    @Transactional
    public void deleteNotification(Long id, String userEmail) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Notification not found"));

        if (!notification.getRecipient().getEmail().equalsIgnoreCase(userEmail)) {
            throw new AccessDeniedException("Not authorized to delete this notification");
        }

        if (!notification.isDeletable()) {
            throw new IllegalArgumentException("This notification cannot be deleted");
        }

        notificationRepository.delete(notification);
    }

    // --- Mapper ---
    private NotificationDTO mapToDTO(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setMessage(notification.getMessage());
        dto.setType(notification.getType());
        dto.setRecipientEmail(notification.getRecipient().getEmail());
        dto.setRead(notification.isRead());
        dto.setDeletable(notification.isDeletable());
        dto.setCreatedAt(notification.getCreatedAt());
        return dto;
    }
}
