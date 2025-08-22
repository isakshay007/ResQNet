package com.resqnet.controller;

import com.resqnet.dto.ContributionDTO;
import com.resqnet.service.ContributionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing Contributions
 * Endpoints allow responders to contribute resources to requests,
 * and fetch contributions globally, by request, or by responder.
 */
@RestController
@RequestMapping("/api/contributions")
@CrossOrigin(origins = "*")  // Allow all frontend clients (React, Angular, etc.)
public class ContributionController {

    private final ContributionService service;

    public ContributionController(ContributionService service) {
        this.service = service;
    }

    /**
     * Create a new contribution (Responder contributes to a resource request).
     *
     * @param dto Contribution data (validated)
     * @return saved ContributionDTO
     */
    @PostMapping
    public ContributionDTO createContribution(@Valid @RequestBody ContributionDTO dto) {
        return service.createContribution(dto);
    }

    /**
     * Get all contributions in the system.
     *
     * @return list of ContributionDTO
     */
    @GetMapping
    public List<ContributionDTO> getAllContributions() {
        return service.getAllContributions();
    }

    /**
     * Get contributions made for a specific request.
     *
     * @param requestId ID of the resource request
     * @return list of contributions for that request
     */
    @GetMapping("/request/{requestId}")
    public List<ContributionDTO> getByRequest(@PathVariable Long requestId) {
        return service.getByRequest(requestId);
    }

    /**
     * Get contributions made by a specific responder (by email).
     *
     * @param responderEmail responder's email
     * @return list of contributions by that responder
     */
    @GetMapping("/responder/{responderEmail}")
    public List<ContributionDTO> getByResponder(@PathVariable String responderEmail) {
        return service.getByResponder(responderEmail);
    }
}
