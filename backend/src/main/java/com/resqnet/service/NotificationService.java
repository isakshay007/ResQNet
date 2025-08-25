package com.resqnet.service;

import com.resqnet.dto.NotificationDTO;
import com.resqnet.model.Notification;
import com.resqnet.model.User;
import com.resqnet.repository.NotificationRepository;
import com.resqnet.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    //  Save a new notification
    @Transactional
    public NotificationDTO saveNotification(NotificationDTO dto) {
        User user = userRepository.findByEmail(dto.getRecipientEmail())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Notification notification = new Notification(
                dto.getMessage(),
                dto.getType() != null ? dto.getType() : "SYSTEM",
                user,
                dto.isDeletable()
        );

        Notification saved = notificationRepository.save(notification);
        return mapToDTO(saved);
    }

    //  Fetch all notifications for a user
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

    //  Mark notification as read
    @Transactional
    public void markAsRead(Long id, String userEmail) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Notification not found"));

        if (!notification.getRecipient().getEmail().equalsIgnoreCase(userEmail)) {
            throw new IllegalArgumentException("Not authorized to update this notification");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    //  Delete notification
    @Transactional
    public void deleteNotification(Long id, String userEmail) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Notification not found"));

        if (!notification.getRecipient().getEmail().equalsIgnoreCase(userEmail)) {
            throw new IllegalArgumentException("Not authorized to delete this notification");
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
