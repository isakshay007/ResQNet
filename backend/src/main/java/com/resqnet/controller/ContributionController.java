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

    // --- RESPONDER: Create a contribution
    @PostMapping
    @PreAuthorize("hasRole('RESPONDER')")
    public ResponseEntity<ContributionDTO> createContribution(@Valid @RequestBody ContributionDTO dto,
                                                              Authentication auth) {
        ContributionDTO created = service.createContribution(dto, auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // --- ADMIN: Get all contributions
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<ContributionDTO> getAllContributions() {
        return service.getAllContributions();
    }

    // --- REPORTER (own requests), RESPONDER (any), ADMIN (any)
    @GetMapping("/request/{requestId}")
    @PreAuthorize("isAuthenticated()")
    public List<ContributionDTO> getByRequest(@PathVariable Long requestId,
                                              Authentication auth) {
        return service.getByRequestWithSecurity(requestId, auth.getName());
    }

    // --- RESPONDER (own), ADMIN (any)
    @GetMapping("/responder/{responderEmail}")
    @PreAuthorize("isAuthenticated()")
    public List<ContributionDTO> getByResponder(@PathVariable String responderEmail,
                                                Authentication auth) {
        return service.getByResponderWithSecurity(responderEmail, auth.getName());
    }

    // --- ADMIN or RESPONDER (own): Delete a contribution
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteContribution(@PathVariable Long id) {
        service.deleteContribution(id);
        return ResponseEntity.noContent().build();
    }
}
