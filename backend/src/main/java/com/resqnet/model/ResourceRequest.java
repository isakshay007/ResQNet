package com.resqnet.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "resource_requests")
public class ResourceRequest {

    public enum Status {
        PENDING,    // newly created, no contributions yet
        PARTIAL,    // some contributions but not fulfilled
        FULFILLED   // fully satisfied
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String category; // e.g. water, food, shelter, medical

    @Column(nullable = false)
    private int requestedQuantity;

    @Column(nullable = false)
    private int fulfilledQuantity = 0;

    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "disaster_id", nullable = false)
    private Disaster disaster;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    // One ResourceRequest → Many Contributions
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
        } else if (this.fulfilledQuantity > 0) {
            this.status = Status.PARTIAL;
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
        updateStatus(); // keep status consistent
    }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public Disaster getDisaster() { return disaster; }
    public void setDisaster(Disaster disaster) { this.disaster = disaster; }

    public User getReporter() { return reporter; }
    public void setReporter(User reporter) { this.reporter = reporter; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public List<Contribution> getContributions() { return contributions; }
    public void setContributions(List<Contribution> contributions) { this.contributions = contributions; }
}
