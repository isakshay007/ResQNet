package com.resqnet.controller;

import com.resqnet.dto.ContributionDTO;
import com.resqnet.service.ContributionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contributions")
@CrossOrigin(origins = "*")
public class ContributionController {

    private final ContributionService service;

    public ContributionController(ContributionService service) {
        this.service = service;
    }

    // ---------------- RESPONDER ----------------

    // Create a new contribution (Responder only)
    @PostMapping
    @PreAuthorize("hasRole('RESPONDER')")
    public ResponseEntity<ContributionDTO> createContribution(@Valid @RequestBody ContributionDTO dto,
                                                              Authentication auth) {
        ContributionDTO created = service.createContribution(dto, auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // ---------------- COMMON (Reporter, Responder, Admin) ----------------

    // Global view of contributions
    // - Admin → all contributions
    // - Responder → only their contributions
    // - Reporter → contributions tied to their requests
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<ContributionDTO> getAllContributions(Authentication auth) {
        return service.getAllContributionsForUser(auth.getName());
    }

    // By request
    // - Admin → any request
    // - Responder → any request
    // - Reporter → only if the request belongs to them
    @GetMapping("/request/{requestId}")
    @PreAuthorize("isAuthenticated()")
    public List<ContributionDTO> getByRequest(@PathVariable Long requestId,
                                              Authentication auth) {
        return service.getByRequestWithSecurity(requestId, auth.getName());
    }

    // By responder
    // - Admin → any responder
    // - Responder → only themselves
    // - Reporter → forbidden
    @GetMapping("/responder/{responderEmail}")
    @PreAuthorize("isAuthenticated()")
    public List<ContributionDTO> getByResponder(@PathVariable String responderEmail,
                                                Authentication auth) {
        return service.getByResponderWithSecurity(responderEmail, auth.getName());
    }

    // ---------------- ADMIN / RESPONDER ----------------

    // Delete contribution
    // - Admin → any contribution
    // - Responder → only their own (checked in service)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONDER')")
    public ResponseEntity<Void> deleteContribution(@PathVariable Long id,
                                                   Authentication auth) {
        service.deleteContributionWithSecurity(id, auth.getName());
        return ResponseEntity.noContent().build();
    }
}
