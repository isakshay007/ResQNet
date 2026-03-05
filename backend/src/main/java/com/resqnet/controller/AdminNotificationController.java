package com.resqnet.controller;

import com.resqnet.dto.NotificationDTO;
import com.resqnet.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/notifications")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Notifications")
public class AdminNotificationController {

    private final NotificationService service;

    public AdminNotificationController(NotificationService service) {
        this.service = service;
    }

    @Operation(summary = "Get all admin broadcast notifications")
    @GetMapping
    public List<NotificationDTO> getAdminNotifications() {
        return service.getAdminNotifications();
    }

    @Operation(summary = "Delete an admin broadcast notification")
    @DeleteMapping("/{id}")
    public void deleteNotification(@PathVariable Long id) {
        service.deleteNotification(id, null, true); // pass null email, override as admin
    }
}
