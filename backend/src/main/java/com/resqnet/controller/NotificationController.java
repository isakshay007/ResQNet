package com.resqnet.controller;

import com.resqnet.dto.NotificationDTO;
import com.resqnet.service.NotificationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*") // allow frontend apps
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    //  Get all notifications for logged-in user
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<NotificationDTO> getNotifications(Authentication auth) {
        return service.getNotifications(auth.getName());
    }

    //  Get only unread notifications
    @GetMapping("/unread")
    @PreAuthorize("isAuthenticated()")
    public List<NotificationDTO> getUnreadNotifications(Authentication auth) {
        return service.getUnreadNotifications(auth.getName());
    }

    // Mark a notification as read
    @PutMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public void markAsRead(@PathVariable Long id, Authentication auth) {
        service.markAsRead(id, auth.getName());
    }

    //  Delete a notification (only if deletable)
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public void deleteNotification(@PathVariable Long id, Authentication auth) {
        service.deleteNotification(id, auth.getName());
    }
}
