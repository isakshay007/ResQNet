package com.resqnet.dto;

import com.resqnet.model.ResourceRequest;
import java.time.LocalDateTime;

public class ResourceRequestDTO {
    private Long id;
    private String category;
    private int requestedQuantity;
    private int fulfilledQuantity;
    private ResourceRequest.Status status;
    private Long disasterId;
    private String reporterEmail; // instead of reporterId
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
