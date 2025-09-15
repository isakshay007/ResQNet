package com.resqnet.controller;

import com.resqnet.dto.NotificationDTO;
import com.resqnet.service.NotificationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/notifications")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')") // applies to all endpoints
public class AdminNotificationController {

    private final NotificationService service;

    public AdminNotificationController(NotificationService service) {
        this.service = service;
    }

    //  Get all admin broadcast/system notifications
    @GetMapping
    public List<NotificationDTO> getAdminNotifications() {
        return service.getAdminNotifications();
    }

    //  Delete an admin broadcast notification (admin override)
    @DeleteMapping("/{id}")
    public void deleteNotification(@PathVariable Long id) {
        service.deleteNotification(id, null, true); // pass null email, override as admin
    }
}
