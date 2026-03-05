package com.resqnet.controller;

import com.resqnet.dto.ContributionDTO;
import com.resqnet.service.ContributionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Contributions")
public class ContributionController {

    private final ContributionService service;

    public ContributionController(ContributionService service) {
        this.service = service;
    }

    @Operation(summary = "Create a contribution (Responder only)")
    @PostMapping
    @PreAuthorize("hasRole('RESPONDER')")
    public ResponseEntity<ContributionDTO> createContribution(@Valid @RequestBody ContributionDTO dto,
                                                              Authentication auth) {
        ContributionDTO created = service.createContribution(dto, auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Get contributions filtered by the current user's role")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<ContributionDTO> getAllContributions(Authentication auth) {
        return service.getAllContributionsForUser(auth.getName());
    }

    @Operation(summary = "Get contributions for a specific resource request")
    @GetMapping("/request/{requestId}")
    @PreAuthorize("isAuthenticated()")
    public List<ContributionDTO> getByRequest(@PathVariable Long requestId,
                                              Authentication auth) {
        return service.getByRequestWithSecurity(requestId, auth.getName());
    }

    @Operation(summary = "Get contributions by a specific responder's email")
    @GetMapping("/responder/{responderEmail}")
    @PreAuthorize("isAuthenticated()")
    public List<ContributionDTO> getByResponder(@PathVariable String responderEmail,
                                                Authentication auth) {
        return service.getByResponderWithSecurity(responderEmail, auth.getName());
    }

    @Operation(summary = "Delete a contribution (Admin or owning Responder)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONDER')")
    public ResponseEntity<Void> deleteContribution(@PathVariable Long id,
                                                   Authentication auth) {
        service.deleteContributionWithSecurity(id, auth.getName());
        return ResponseEntity.noContent().build();
    }
}
