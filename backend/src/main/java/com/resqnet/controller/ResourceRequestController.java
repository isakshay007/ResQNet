package com.resqnet.controller;

import com.resqnet.dto.ResourceRequestDTO;
import com.resqnet.service.ResourceRequestService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing Resource Requests.
 * - Reporters can create requests linked to disasters.
 * - Reporters can only fetch their own requests.
 * - Admin/Responders can fetch all requests for dashboard use.
 */
@RestController
@RequestMapping("/api/requests")
@CrossOrigin(origins = "*") // Allow frontend access
public class ResourceRequestController {

    private final ResourceRequestService service;

    public ResourceRequestController(ResourceRequestService service) {
        this.service = service;
    }

    /**
     * Create a new resource request (Reporter action).
     *  We no longer trust `reporterEmail` from the DTO.
     * Always take reporter email from Authentication.
     */
    @PostMapping
    @PreAuthorize("hasRole('REPORTER')")
    public ResourceRequestDTO createRequest(@Valid @RequestBody ResourceRequestDTO dto,
                                            Authentication auth) {
        return service.createRequest(dto, auth.getName());
    }

    /**
     * Reporter: Fetch all of their own requests.
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('REPORTER')")
    public List<ResourceRequestDTO> getMyRequests(Authentication auth) {
        return service.getRequestsForReporter(auth.getName());
    }

    /**
     * Reporter: Fetch a specific request by ID (only if owned).
     * Restrict {id} to digits so it won't clash with "/my".
     */
    @GetMapping("/{id:[0-9]+}")
    @PreAuthorize("hasRole('REPORTER')")
    public ResourceRequestDTO getMyRequestById(@PathVariable Long id, Authentication auth) {
        return service.getRequestByIdForReporter(id, auth.getName());
    }

    /**
     * Admin/Responder: Fetch all resource requests.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','RESPONDER')")
    public List<ResourceRequestDTO> getAllRequests() {
        return service.getAllRequests();
    }
}
