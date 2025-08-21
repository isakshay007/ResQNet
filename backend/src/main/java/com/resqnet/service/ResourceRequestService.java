package com.resqnet.service;

import com.resqnet.dto.ResourceRequestDTO;
import com.resqnet.model.Disaster;
import com.resqnet.model.ResourceRequest;
import com.resqnet.model.User;
import com.resqnet.repository.DisasterRepository;
import com.resqnet.repository.ResourceRequestRepository;
import com.resqnet.repository.UserRepository;
import org.springframework.stereotype.Service;

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

    // --- Create request from DTO ---
    public ResourceRequestDTO createRequest(ResourceRequestDTO dto) {
        ResourceRequest request = new ResourceRequest();
        request.setCategory(dto.getCategory());
        request.setRequestedQuantity(dto.getRequestedQuantity());

        // Always start with 0 fulfilled & PENDING
        request.setFulfilledQuantity(0);
        request.setStatus(ResourceRequest.Status.PENDING);

        if (dto.getDisasterId() != null) {
            Disaster disaster = disasterRepository.findById(dto.getDisasterId())
                    .orElseThrow(() -> new RuntimeException("Disaster not found"));
            request.setDisaster(disaster);
        }

        if (dto.getReporterEmail() != null) {
            User reporter = userRepository.findByEmail(dto.getReporterEmail())
                    .orElseThrow(() -> new RuntimeException("Reporter not found"));
            request.setReporter(reporter);
        }

        ResourceRequest saved = resourceRequestRepository.save(request);
        return mapToDTO(saved);
    }

    // --- Get all as DTOs ---
    public List<ResourceRequestDTO> getAllRequests() {
        return resourceRequestRepository.findAll().stream()
                .map(this::mapToDTO)
                .toList();
    }

    // --- Get one as DTO ---
    public ResourceRequestDTO getRequestById(Long id) {
        return resourceRequestRepository.findById(id)
                .map(this::mapToDTO)
                .orElse(null);
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
        dto.setReporterEmail(r.getReporter() != null ? r.getReporter().getEmail() : null); // ✅ email instead of id
        return dto;
    }
}
