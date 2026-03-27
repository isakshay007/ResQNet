package com.resqnet.service;

import com.resqnet.dto.NotificationDTO;
import com.resqnet.model.Notification;
import com.resqnet.model.User;
import com.resqnet.repository.AdminNotificationReadRepository;
import com.resqnet.repository.NotificationRepository;
import com.resqnet.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock private AdminNotificationReadRepository adminNotificationReadRepository;
    @Mock private NotificationRepository notificationRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private NotificationService service;

    private User user;
    private User adminUser;
    private Notification sampleNotification;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setName("User");
        user.setRole(User.Role.REPORTER);

        adminUser = new User();
        adminUser.setId(2L);
        adminUser.setEmail("admin@example.com");
        adminUser.setName("Admin");
        adminUser.setRole(User.Role.ADMIN);

        sampleNotification = new Notification("Test message", "SYSTEM", user, true);
        sampleNotification.setId(1L);
        sampleNotification.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void saveNotification_userSpecific_persistsAndReturnsDTO() {
        NotificationDTO dto = new NotificationDTO();
        dto.setRecipientEmail("user@example.com");
        dto.setMessage("Test message");
        dto.setType("SYSTEM");
        dto.setDeletable(true);
        dto.setAdminBroadcast(false);

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(notificationRepository.save(any(Notification.class))).thenReturn(sampleNotification);

        NotificationDTO result = service.saveNotification(dto);

        assertNotNull(result);
        assertEquals("Test message", result.getMessage());
        assertEquals("SYSTEM", result.getType());
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void saveNotification_adminBroadcast_persistsWithoutRecipient() {
        Notification broadcastNotif = new Notification("Admin alert", "ADMIN_LOG", false);
        broadcastNotif.setId(2L);
        broadcastNotif.setCreatedAt(LocalDateTime.now());

        NotificationDTO dto = new NotificationDTO();
        dto.setMessage("Admin alert");
        dto.setType("ADMIN_LOG");
        dto.setDeletable(false);
        dto.setAdminBroadcast(true);

        when(notificationRepository.save(any(Notification.class))).thenReturn(broadcastNotif);

        NotificationDTO result = service.saveNotification(dto);

        assertNotNull(result);
        assertTrue(result.isAdminBroadcast());
        assertEquals("ADMIN-BROADCAST", result.getRecipientEmail());
        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    void getNotifications_returnsUserNotifications() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(notificationRepository.findByRecipientOrderByCreatedAtDesc(user))
                .thenReturn(List.of(sampleNotification));

        List<NotificationDTO> result = service.getNotifications("user@example.com");

        assertFalse(result.isEmpty());
        assertEquals("Test message", result.get(0).getMessage());
    }

    @Test
    void getUnreadNotifications_returnsOnlyUnread() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(notificationRepository.findByRecipientAndReadFalseOrderByCreatedAtDesc(user))
                .thenReturn(List.of(sampleNotification));

        List<NotificationDTO> result = service.getUnreadNotifications("user@example.com");

        assertEquals(1, result.size());
        assertFalse(result.get(0).isRead());
    }

    @Test
    void markAsRead_setsReadFlagAndSaves() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(sampleNotification));
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        service.markAsRead(1L, "user@example.com");

        assertTrue(sampleNotification.isRead());
        verify(notificationRepository).save(sampleNotification);
    }

    @Test
    void markAsRead_adminBroadcast_tracksReadPerAdmin() {
        Notification adminBroadcast = new Notification("Admin alert", "ADMIN_LOG", false);
        adminBroadcast.setId(2L);
        adminBroadcast.setCreatedAt(LocalDateTime.now());

        when(notificationRepository.findById(2L)).thenReturn(Optional.of(adminBroadcast));
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser));
        when(adminNotificationReadRepository.findByNotificationAndAdmin(adminBroadcast, adminUser))
                .thenReturn(Optional.empty());

        service.markAsRead(2L, "admin@example.com");

        verify(adminNotificationReadRepository).save(any());
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void deleteNotification_whenDeletable_removes() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(sampleNotification));

        service.deleteNotification(1L, "user@example.com", false);

        verify(adminNotificationReadRepository).deleteByNotification(sampleNotification);
        verify(notificationRepository).delete(sampleNotification);
    }

    @Test
    void deleteNotification_whenNotAuthorized_throwsAccessDenied() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(sampleNotification));

        assertThrows(AccessDeniedException.class,
                () -> service.deleteNotification(1L, "other@example.com", false));
    }
}
