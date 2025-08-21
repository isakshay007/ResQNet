package com.resqnet.dto;

import java.time.LocalDateTime;

public class ContributionDTO {
    private Long id;
    private int contributedQuantity;
    private Long requestId;
    private String responderEmail; //  instead of responderId
    private LocalDateTime updatedAt;

    // --- Getters & Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public int getContributedQuantity() { return contributedQuantity; }
    public void setContributedQuantity(int contributedQuantity) { this.contributedQuantity = contributedQuantity; }

    public Long getRequestId() { return requestId; }
    public void setRequestId(Long requestId) { this.requestId = requestId; }

    public String getResponderEmail() { return responderEmail; }
    public void setResponderEmail(String responderEmail) { this.responderEmail = responderEmail; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
