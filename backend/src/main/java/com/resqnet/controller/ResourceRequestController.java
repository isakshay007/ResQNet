package com.resqnet.controller;

import com.resqnet.dto.ResourceRequestDTO;
import com.resqnet.service.ResourceRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/requests")
@Tag(name = "Resource Requests")
public class ResourceRequestController {

    private final ResourceRequestService service;

    public ResourceRequestController(ResourceRequestService service) {
        this.service = service;
    }

    @Operation(summary = "Create a resource request (Reporter only)")
    @PostMapping
    @PreAuthorize("hasRole('REPORTER')")
    public ResponseEntity<ResourceRequestDTO> createRequest(
            @Valid @RequestBody ResourceRequestDTO dto,
            Authentication auth) {
        ResourceRequestDTO created = service.createRequest(dto, auth.getName());
        return ResponseEntity.created(URI.create("/api/requests/" + created.getId()))
                             .body(created);
    }

    @Operation(summary = "Get current reporter's own requests")
    @GetMapping("/my")
    @PreAuthorize("hasRole('REPORTER')")
    public List<ResourceRequestDTO> getMyRequests(Authentication auth) {
        return service.getRequestsForReporter(auth.getName());
    }

    @Operation(summary = "Get a specific request owned by the current reporter")
    @GetMapping("/my/{id:[0-9]+}")
    @PreAuthorize("hasRole('REPORTER')")
    public ResourceRequestDTO getMyRequestById(@PathVariable Long id, Authentication auth) {
        return service.getRequestByIdForReporter(id, auth.getName());
    }

    @Operation(summary = "Get all resource requests")
    @GetMapping
    @PreAuthorize("hasAnyRole('REPORTER','RESPONDER','ADMIN')")
    public List<ResourceRequestDTO> getAllRequests() {
        return service.getAllRequests();
    }

    @Operation(summary = "Get a resource request by ID")
    @GetMapping("/{id:[0-9]+}")
    @PreAuthorize("hasAnyRole('REPORTER','RESPONDER','ADMIN')")
    public ResourceRequestDTO getRequestById(@PathVariable Long id) {
        return service.getRequestById(id);
    }

    @Operation(summary = "Update a resource request (Admin only)")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResourceRequestDTO updateRequest(@PathVariable Long id,
                                            @Valid @RequestBody ResourceRequestDTO dto) {
        dto.setId(id);
        return service.updateRequest(dto);
    }

    @Operation(summary = "Delete a resource request (Admin only)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteRequest(@PathVariable Long id) {
        service.deleteRequest(id);
        return ResponseEntity.noContent().build();
    }
}
