package com.resqnet.controller;

import com.resqnet.dto.NotificationDTO;
import com.resqnet.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notifications")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @Operation(summary = "Get all notifications for the current user")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<NotificationDTO> getNotifications(Authentication auth) {
        return service.getNotifications(auth.getName());
    }

    @Operation(summary = "Get only unread notifications")
    @GetMapping("/unread")
    @PreAuthorize("isAuthenticated()")
    public List<NotificationDTO> getUnreadNotifications(Authentication auth) {
        return service.getUnreadNotifications(auth.getName());
    }

    @Operation(summary = "Mark a notification as read")
    @PutMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public void markAsRead(@PathVariable Long id, Authentication auth) {
        service.markAsRead(id, auth.getName());
    }

    @Operation(summary = "Delete a notification")
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public void deleteNotification(@PathVariable Long id, Authentication auth) {
        service.deleteNotification(id, auth.getName(), false); // normal user delete, no override
    }
}
