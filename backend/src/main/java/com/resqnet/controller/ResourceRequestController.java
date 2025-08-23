package com.resqnet.controller;

import com.resqnet.dto.ResourceRequestDTO;
import com.resqnet.service.ResourceRequestService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing Resource Requests.
 * - Reporters can create requests linked to disasters.
 * - Frontend fetches requests for map display and dashboard use.
 */
@RestController
@RequestMapping("/api/requests")
@CrossOrigin(origins = "*") //  Allow frontend access
public class ResourceRequestController {

    private final ResourceRequestService service;

    public ResourceRequestController(ResourceRequestService service) {
        this.service = service;
    }

    /**
     * Create a new resource request (Reporter action).
     *
     * Only REPORTERs are allowed.
     */
    @PostMapping
    @PreAuthorize("hasRole('REPORTER')")
    public ResourceRequestDTO createRequest(@Valid @RequestBody ResourceRequestDTO dto) {
        return service.createRequest(dto);
    }

    /**
     * Fetch all resource requests (for map/dashboard display).
     *
     * Open to all authenticated users (could restrict further if needed).
     */
    @GetMapping
    public List<ResourceRequestDTO> getAllRequests() {
        return service.getAllRequests();
    }

    /**
     * Fetch a specific request by ID.
     *
     * Open to all authenticated users.
     */
    @GetMapping("/{id}")
    public ResourceRequestDTO getRequestById(@PathVariable Long id) {
        return service.getRequestById(id);
    }
}
