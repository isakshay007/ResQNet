package com.resqnet.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class ContributionDTO {
    private Long id;

    @Min(value = 1, message = "Contribution quantity must be at least 1")
    private int contributedQuantity;

    @NotNull(message = "Request ID is required")
    private Long requestId;

    @NotBlank(message = "Responder email is required")
    private String responderEmail; // instead of responderId

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
