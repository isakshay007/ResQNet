package com.resqnet.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "admin_notification_reads",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"notification_id", "admin_id"})
        }
)
public class AdminNotificationRead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "notification_id", nullable = false)
    private Notification notification;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "admin_id", nullable = false)
    private User admin;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime readAt;

    public AdminNotificationRead() {
    }

    public AdminNotificationRead(Notification notification, User admin) {
        this.notification = notification;
        this.admin = admin;
    }

    public Long getId() {
        return id;
    }

    public Notification getNotification() {
        return notification;
    }

    public void setNotification(Notification notification) {
        this.notification = notification;
    }

    public User getAdmin() {
        return admin;
    }

    public void setAdmin(User admin) {
        this.admin = admin;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }
}
