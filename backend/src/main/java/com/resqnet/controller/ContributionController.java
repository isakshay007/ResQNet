package com.resqnet.controller;

import com.resqnet.dto.ContributionDTO;
import com.resqnet.service.ContributionService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing Contributions
 *
 * Role rules:
 * - RESPONDER: can create contributions, see ALL contributions for ANY request,
 *              and view only their own contributions by responderEmail.
 * - REPORTER: can only view contributions made to their OWN resource requests.
 * - ADMIN: has full access to all contributions.
 */
@RestController
@RequestMapping("/api/contributions")
@CrossOrigin(origins = "*") // Allow frontend apps
public class ContributionController {

    private final ContributionService service;

    public ContributionController(ContributionService service) {
        this.service = service;
    }

    /**
     * Create a new contribution.
     * Allowed: RESPONDER
     */
    @PostMapping
    @PreAuthorize("hasRole('RESPONDER')")
    public ContributionDTO createContribution(@Valid @RequestBody ContributionDTO dto,
                                              Authentication auth) {
        return service.createContribution(dto, auth.getName());
    }

    /**
     * Get all contributions in the system.
     * Allowed: ADMIN only
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<ContributionDTO> getAllContributions() {
        return service.getAllContributions();
    }

    /**
     * Get contributions made for a specific resource request.
     *
     * Allowed:
     * - REPORTER → only for their own requests.
     * - RESPONDER → can view ALL contributions for ANY request.
     * - ADMIN → unrestricted.
     */
    @GetMapping("/request/{requestId}")
    @PreAuthorize("isAuthenticated()")
    public List<ContributionDTO> getByRequest(@PathVariable Long requestId,
                                              Authentication auth) {
        return service.getByRequestWithSecurity(requestId, auth.getName());
    }

    /**
     * Get contributions made by a specific responder.
     *
     * Allowed:
     * - RESPONDER → only their own contributions.
     * - ADMIN → unrestricted.
     */
    @GetMapping("/responder/{responderEmail}")
    @PreAuthorize("isAuthenticated()")
    public List<ContributionDTO> getByResponder(@PathVariable String responderEmail,
                                                Authentication auth) {
        return service.getByResponderWithSecurity(responderEmail, auth.getName());
    }
}
