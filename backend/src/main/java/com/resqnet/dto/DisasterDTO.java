package com.resqnet.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.List;

public class DisasterDTO {
    private Long id;

    @NotBlank(message = "Disaster type is required")
    private String type;

    @NotBlank(message = "Severity is required")
    private String severity;

    @NotBlank(message = "Description is required")
    private String description;

    @DecimalMin(value = "-90.0", message = "Latitude must be >= -90")
    @DecimalMax(value = "90.0", message = "Latitude must be <= 90")
    private double latitude;

    @DecimalMin(value = "-180.0", message = "Longitude must be >= -180")
    @DecimalMax(value = "180.0", message = "Longitude must be <= 180")
    private double longitude;

    //  Reporter info
    @JsonProperty(access = Access.READ_ONLY)
    private String reporterEmail;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty(access = Access.READ_ONLY)
    private String reporterName; // helpful for popup display

    // New: created timestamp
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty(access = Access.READ_ONLY)
    private LocalDateTime createdAt;

    // Status flow (computed in service)
    @JsonProperty(access = Access.READ_ONLY)
    private String status; // "reported", "partial", "fulfilled"

    //  Contribution summary for UI
    @JsonProperty(access = Access.READ_ONLY)
    private List<String> contributions; // e.g. ["food", "water"]

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

    public String getReporterEmail() { return reporterEmail; }
    public void setReporterEmail(String reporterEmail) { this.reporterEmail = reporterEmail; }

    public String getReporterName() { return reporterName; }
    public void setReporterName(String reporterName) { this.reporterName = reporterName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<String> getContributions() { return contributions; }
    public void setContributions(List<String> contributions) { this.contributions = contributions; }
}
