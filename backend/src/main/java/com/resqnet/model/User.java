package com.resqnet.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "users",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "email")
    }
)
public class User {

    public enum Role {
        REPORTER,   // Victim / Citizen (green pin on map)
        RESPONDER,  // Volunteer / NGO (blue pin on map)
        ADMIN       // Authority / Government
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String password; // BCrypt hashed

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    // ✅ Permanent map pin (set when the user registers / updates profile)
    @Column
    private Double latitude;

    @Column
    private Double longitude;

    // --- Relationships ---
    // Reporter → Disasters
    @OneToMany(mappedBy = "reporter", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Disaster> disasters = new ArrayList<>();

    // Reporter → Resource Requests
    @OneToMany(mappedBy = "reporter", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<ResourceRequest> resourceRequests = new ArrayList<>();

    // Responder → Contributions
    @OneToMany(mappedBy = "responder", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Contribution> contributions = new ArrayList<>();

    // --- Getters & Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public List<Disaster> getDisasters() { return disasters; }
    public void setDisasters(List<Disaster> disasters) { this.disasters = disasters; }

    public List<ResourceRequest> getResourceRequests() { return resourceRequests; }
    public void setResourceRequests(List<ResourceRequest> resourceRequests) { this.resourceRequests = resourceRequests; }

    public List<Contribution> getContributions() { return contributions; }
    public void setContributions(List<Contribution> contributions) { this.contributions = contributions; }
}
