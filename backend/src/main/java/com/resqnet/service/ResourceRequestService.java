package com.resqnet.service;

import com.resqnet.dto.ResourceRequestDTO;
import com.resqnet.model.Disaster;
import com.resqnet.model.ResourceRequest;
import com.resqnet.model.User;
import com.resqnet.repository.DisasterRepository;
import com.resqnet.repository.ResourceRequestRepository;
import com.resqnet.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;

@Service
public class ResourceRequestService {

    private final ResourceRequestRepository resourceRequestRepository;
    private final DisasterRepository disasterRepository;
    private final UserRepository userRepository;

    public ResourceRequestService(ResourceRequestRepository resourceRequestRepository,
                                  DisasterRepository disasterRepository,
                                  UserRepository userRepository) {
        this.resourceRequestRepository = resourceRequestRepository;
        this.disasterRepository = disasterRepository;
        this.userRepository = userRepository;
    }

    // --- CREATE request (Reporter only, email from Authentication) ---
    @Transactional
    public ResourceRequestDTO createRequest(ResourceRequestDTO dto, String reporterEmail) {
        User reporter = userRepository.findByEmail(reporterEmail)
                .orElseThrow(() -> new EntityNotFoundException("Reporter not found"));

        if (reporter.getRole() != User.Role.REPORTER) {
            throw new AccessDeniedException("Only REPORTER users can create resource requests");
        }

        ResourceRequest request = new ResourceRequest();
        request.setCategory(dto.getCategory());
        request.setRequestedQuantity(dto.getRequestedQuantity());
        request.setFulfilledQuantity(0);
        request.setStatus(ResourceRequest.Status.PENDING);

        if (dto.getDisasterId() != null) {
            Disaster disaster = disasterRepository.findById(dto.getDisasterId())
                    .orElseThrow(() -> new EntityNotFoundException("Disaster not found"));
            request.setDisaster(disaster);
        }

        request.setReporter(reporter);

        ResourceRequest saved = resourceRequestRepository.save(request);
        return mapToDTO(saved);
    }

    // --- READ all (Admin/Responder only) ---
    public List<ResourceRequestDTO> getAllRequests() {
        return resourceRequestRepository.findAll().stream()
                .map(this::mapToDTO)
                .toList();
    }

    // --- READ one (Admin/Responder) ---
    public ResourceRequestDTO getRequestById(Long id) {
        return resourceRequestRepository.findById(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> new EntityNotFoundException("Request not found"));
    }

    // --- Reporter: READ all their own requests ---
    public List<ResourceRequestDTO> getRequestsForReporter(String reporterEmail) {
        return resourceRequestRepository.findByReporter_Email(reporterEmail).stream()
                .map(this::mapToDTO)
                .toList();
    }

    // --- Reporter: READ one of their own requests ---
    public ResourceRequestDTO getRequestByIdForReporter(Long id, String reporterEmail) {
        ResourceRequest req = resourceRequestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Request not found"));

        if (!req.getReporter().getEmail().equalsIgnoreCase(reporterEmail)) {
            throw new AccessDeniedException("You are not authorized to view this request");
        }

        return mapToDTO(req);
    }

    // --- UPDATE request (Admin only) ---
    @Transactional
    public ResourceRequestDTO updateRequest(ResourceRequestDTO dto) {
        ResourceRequest request = resourceRequestRepository.findById(dto.getId())
                .orElseThrow(() -> new EntityNotFoundException("Request not found"));

        request.setCategory(dto.getCategory());
        request.setRequestedQuantity(dto.getRequestedQuantity());
        request.setFulfilledQuantity(dto.getFulfilledQuantity());
        request.setStatus(dto.getStatus());

        if (dto.getDisasterId() != null) {
            Disaster disaster = disasterRepository.findById(dto.getDisasterId())
                    .orElseThrow(() -> new EntityNotFoundException("Disaster not found"));
            request.setDisaster(disaster);
        }

        ResourceRequest updated = resourceRequestRepository.save(request);
        return mapToDTO(updated);
    }

    // --- DELETE request (Admin only) ---
    @Transactional
    public void deleteRequest(Long id) {
        if (!resourceRequestRepository.existsById(id)) {
            throw new EntityNotFoundException("Resource Request not found");
        }
        resourceRequestRepository.deleteById(id);
    }

    // --- Mapping helper ---
    private ResourceRequestDTO mapToDTO(ResourceRequest r) {
        ResourceRequestDTO dto = new ResourceRequestDTO();
        dto.setId(r.getId());
        dto.setCategory(r.getCategory());
        dto.setRequestedQuantity(r.getRequestedQuantity());
        dto.setFulfilledQuantity(r.getFulfilledQuantity());
        dto.setStatus(r.getStatus());
        dto.setCreatedAt(r.getCreatedAt());
        dto.setDisasterId(r.getDisaster() != null ? r.getDisaster().getId() : null);
        dto.setReporterEmail(r.getReporter() != null ? r.getReporter().getEmail() : null);
        return dto;
    }
}
