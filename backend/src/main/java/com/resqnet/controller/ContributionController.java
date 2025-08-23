package com.resqnet.controller;

import com.resqnet.dto.ContributionDTO;
import com.resqnet.service.ContributionService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
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
     * Only RESPONDERs are allowed.
     */
    @PostMapping
    @PreAuthorize("hasRole('RESPONDER')")
    public ContributionDTO createContribution(@Valid @RequestBody ContributionDTO dto) {
        return service.createContribution(dto);
    }

    /**
     * Get all contributions in the system.
     *
     * Open to all authenticated users (can restrict to ADMIN if needed).
     */
    @GetMapping
    public List<ContributionDTO> getAllContributions() {
        return service.getAllContributions();
    }

    /**
     * Get contributions made for a specific request.
     *
     * Open to all authenticated users.
     */
    @GetMapping("/request/{requestId}")
    public List<ContributionDTO> getByRequest(@PathVariable Long requestId) {
        return service.getByRequest(requestId);
    }

    /**
     * Get contributions made by a specific responder (by email).
     *
     * Open to all authenticated users.
     */
    @GetMapping("/responder/{responderEmail}")
    public List<ContributionDTO> getByResponder(@PathVariable String responderEmail) {
        return service.getByResponder(responderEmail);
    }
}
