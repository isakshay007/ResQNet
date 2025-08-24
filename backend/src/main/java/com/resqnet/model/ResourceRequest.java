package com.resqnet.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "resource_requests")
public class ResourceRequest {

    public enum Status {
        PENDING,
        FULFILLED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String category; // e.g. water, food, shelter, medical
    private int requestedQuantity;
    private int fulfilledQuantity = 0;

    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;

    @ManyToOne
    @JoinColumn(name = "disaster_id", nullable = false)
    private Disaster disaster;

    @ManyToOne
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    private LocalDateTime createdAt = LocalDateTime.now();

    // 🔹 One ResourceRequest → Many Contributions
    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Contribution> contributions = new ArrayList<>();

    // --- Business Logic ---
    public void addFulfilledQuantity(int quantity) {
        this.fulfilledQuantity += quantity;
        updateStatus();
    }

    public void updateStatus() {
        if (this.fulfilledQuantity >= this.requestedQuantity) {
            this.status = Status.FULFILLED;
        } else {
            this.status = Status.PENDING;
        }
    }

    // --- Getters & Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getRequestedQuantity() { return requestedQuantity; }
    public void setRequestedQuantity(int requestedQuantity) { this.requestedQuantity = requestedQuantity; }

    public int getFulfilledQuantity() { return fulfilledQuantity; }
    public void setFulfilledQuantity(int fulfilledQuantity) {
        this.fulfilledQuantity = fulfilledQuantity;
        updateStatus(); // always keep status consistent
    }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public Disaster getDisaster() { return disaster; }
    public void setDisaster(Disaster disaster) { this.disaster = disaster; }

    public User getReporter() { return reporter; }
    public void setReporter(User reporter) { this.reporter = reporter; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<Contribution> getContributions() { return contributions; }
    public void setContributions(List<Contribution> contributions) { this.contributions = contributions; }
}
