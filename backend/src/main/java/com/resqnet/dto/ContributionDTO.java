package com.resqnet.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;
import java.util.List;

public class ContributionDTO {
    private Long id;

    @Min(value = 1, message = "Contribution quantity must be at least 1")
    private int contributedQuantity;

    @NotNull(message = "Request ID is required")
    private Long requestId;

    @NotBlank(message = "Category is required")
    @Pattern(regexp = "^(food|water|medical|shelter)$", 
             message = "Category must be one of: food, water, medical, shelter")
    private String category;

    // Responder’s saved location
    private Double latitude;
    private Double longitude;

    // Read-only: backend fills this from Authentication
    @JsonProperty(access = Access.READ_ONLY)
    private String responderEmail;

    // Optional: responder’s display name (frontend popup convenience)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty(access = Access.READ_ONLY)
    private String responderName;

    // Optional: contribution items summary (used for emojis in frontend)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonProperty(access = Access.READ_ONLY)
    private List<String> items;

    // When the contribution was created
    @JsonProperty(access = Access.READ_ONLY)
    private LocalDateTime createdAt;

    // When the contribution was last updated
    @JsonProperty(access = Access.READ_ONLY)
    private LocalDateTime updatedAt;

    // --- Getters & Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public int getContributedQuantity() { return contributedQuantity; }
    public void setContributedQuantity(int contributedQuantity) { this.contributedQuantity = contributedQuantity; }

    public Long getRequestId() { return requestId; }
    public void setRequestId(Long requestId) { this.requestId = requestId; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getResponderEmail() { return responderEmail; }
    public void setResponderEmail(String responderEmail) { this.responderEmail = responderEmail; }

    public String getResponderName() { return responderName; }
    public void setResponderName(String responderName) { this.responderName = responderName; }

    public List<String> getItems() { return items; }
    public void setItems(List<String> items) { this.items = items; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
