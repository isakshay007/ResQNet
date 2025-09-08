package com.resqnet.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.resqnet.model.ResourceRequest;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResourceRequestDTO {
    private Long id;

    @NotBlank(message = "Category is required")
    @Size(max = 100, message = "Category must not exceed 100 characters")
    private String category;

    @Min(value = 1, message = "Requested quantity must be at least 1")
    private int requestedQuantity;

    //  Controlled by backend only
    @JsonProperty(access = Access.READ_ONLY)
    private int fulfilledQuantity;

    //  Status is computed by backend
    @JsonProperty(access = Access.READ_ONLY)
    private ResourceRequest.Status status;

    @NotNull(message = "Disaster ID is required")
    private Long disasterId;

    //  Reporter info handled by backend
    @JsonProperty(access = Access.READ_ONLY)
    private String reporterEmail;

    //  Timestamp set by backend
    @JsonProperty(access = Access.READ_ONLY)
    private LocalDateTime createdAt;

    // --- Getters & Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getRequestedQuantity() { return requestedQuantity; }
    public void setRequestedQuantity(int requestedQuantity) { this.requestedQuantity = requestedQuantity; }

    public int getFulfilledQuantity() { return fulfilledQuantity; }
    public void setFulfilledQuantity(int fulfilledQuantity) { this.fulfilledQuantity = fulfilledQuantity; }

    public ResourceRequest.Status getStatus() { return status; }
    public void setStatus(ResourceRequest.Status status) { this.status = status; }

    public Long getDisasterId() { return disasterId; }
    public void setDisasterId(Long disasterId) { this.disasterId = disasterId; }

    public String getReporterEmail() { return reporterEmail; }
    public void setReporterEmail(String reporterEmail) { this.reporterEmail = reporterEmail; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
