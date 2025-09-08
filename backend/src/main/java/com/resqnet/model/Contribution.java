package com.resqnet.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "contributions")
public class Contribution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Min(value = 1, message = "Contribution must be at least 1")
    @Column(nullable = false)
    private int contributedQuantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private ResourceRequest request;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responder_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User responder;

    @NotBlank(message = "Category is required")
    @Column(nullable = false, length = 100)
    private String category;

    @NotNull(message = "Latitude is required")
    @Column(nullable = false)
    private Double latitude;

    @NotNull(message = "Longitude is required")
    @Column(nullable = false)
    private Double longitude;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // --- Getters & Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public int getContributedQuantity() { return contributedQuantity; }
    public void setContributedQuantity(int contributedQuantity) { this.contributedQuantity = contributedQuantity; }

    public ResourceRequest getRequest() { return request; }
    public void setRequest(ResourceRequest request) { this.request = request; }

    public User getResponder() { return responder; }
    public void setResponder(User responder) { this.responder = responder; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
