package com.resqnet.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Notification message
    @Column(nullable = false, length = 500)
    private String message;

    // Notification type (REQUEST, CONTRIBUTION, DISASTER, USER, SYSTEM, ADMIN)
    @Column(nullable = false)
    private String type;

    // Recipient user (normal user notification)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User recipient;

    // Is this meant for all admins? (true = broadcast to admins)
    @Column(nullable = false)
    private boolean adminBroadcast = false;

    // Has the user read the notification?
    @Column(nullable = false)
    private boolean read = false;

    // Can user delete the notification? (true = deletable, false = system permanent log)
    @Column(nullable = false)
    private boolean deletable = true;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    // --- Constructors ---
    public Notification() {}

    // For user-specific notification
    public Notification(String message, String type, User recipient, boolean deletable) {
        this.message = message;
        this.type = type;
        this.recipient = recipient;
        this.deletable = deletable;
        this.adminBroadcast = false;
    }

    // For admin broadcast notification
    public Notification(String message, String type, boolean deletable) {
        this.message = message;
        this.type = type;
        this.deletable = deletable;
        this.adminBroadcast = true;
    }

    // --- Getters & Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public User getRecipient() { return recipient; }
    public void setRecipient(User recipient) { this.recipient = recipient; }

    public boolean isAdminBroadcast() { return adminBroadcast; }
    public void setAdminBroadcast(boolean adminBroadcast) { this.adminBroadcast = adminBroadcast; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    public boolean isDeletable() { return deletable; }
    public void setDeletable(boolean deletable) { this.deletable = deletable; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
