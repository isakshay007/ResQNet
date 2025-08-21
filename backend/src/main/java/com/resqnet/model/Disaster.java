package com.resqnet.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "disasters")
public class Disaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;       // e.g. Flood, Earthquake, Fire
    private String severity;   // e.g. LOW, MEDIUM, HIGH
    private String description;

    private double latitude;
    private double longitude;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reporter_id")
    
    private User reporter;     // Who reported this disaster

    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    // --- Lifecycle Hook to Auto-Set createdAt ---
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // --- Getters & Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public User getReporter() { return reporter; }
    public void setReporter(User reporter) { this.reporter = reporter; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
