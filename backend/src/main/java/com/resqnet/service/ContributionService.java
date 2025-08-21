package com.resqnet.service;

import com.resqnet.dto.ContributionDTO;
import com.resqnet.model.Contribution;
import com.resqnet.model.ResourceRequest;
import com.resqnet.model.User;
import com.resqnet.repository.ContributionRepository;
import com.resqnet.repository.ResourceRequestRepository;
import com.resqnet.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContributionService {

    private final ContributionRepository contributionRepository;
    private final ResourceRequestRepository requestRepository;
    private final UserRepository userRepository;

    public ContributionService(ContributionRepository contributionRepository,
                               ResourceRequestRepository requestRepository,
                               UserRepository userRepository) {
        this.contributionRepository = contributionRepository;
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
    }

    // --- Create contribution ---
    public ContributionDTO createContribution(ContributionDTO dto) {
        ResourceRequest request = requestRepository.findById(dto.getRequestId())
                .orElseThrow(() -> new RuntimeException("Resource Request not found"));

        User responder = userRepository.findByEmail(dto.getResponderEmail())
                .orElseThrow(() -> new RuntimeException("Responder not found"));

        Contribution contribution = new Contribution();
        contribution.setContributedQuantity(dto.getContributedQuantity());
        contribution.setRequest(request);
        contribution.setResponder(responder);

        // Update request fulfillment
        request.addFulfilledQuantity(dto.getContributedQuantity());
        requestRepository.save(request);

        Contribution saved = contributionRepository.save(contribution);
        return mapToDTO(saved);
    }

    // --- Get all contributions ---
    public List<ContributionDTO> getAllContributions() {
        return contributionRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // --- Get contributions by request ---
    public List<ContributionDTO> getByRequest(Long requestId) {
        return contributionRepository.findByRequestId(requestId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // --- Get contributions by responder (email-based now) ---
    public List<ContributionDTO> getByResponder(String responderEmail) {
        return contributionRepository.findByResponderEmail(responderEmail).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // --- Helper: map entity → DTO ---
    private ContributionDTO mapToDTO(Contribution c) {
        ContributionDTO dto = new ContributionDTO();
        dto.setId(c.getId());
        dto.setContributedQuantity(c.getContributedQuantity());
        dto.setRequestId(c.getRequest().getId());
        dto.setResponderEmail(c.getResponder().getEmail()); //  email instead of id
        dto.setUpdatedAt(c.getUpdatedAt());
        return dto;
    }
}
