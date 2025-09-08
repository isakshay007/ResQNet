package com.resqnet.controller;

import com.resqnet.dto.ResourceRequestDTO;
import com.resqnet.service.ResourceRequestService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/requests")
@CrossOrigin(origins = "*")
public class ResourceRequestController {

    private final ResourceRequestService service;

    public ResourceRequestController(ResourceRequestService service) {
        this.service = service;
    }

    // --- Reporter endpoints ---

    @PostMapping
    @PreAuthorize("hasRole('REPORTER')")
    public ResponseEntity<ResourceRequestDTO> createRequest(@Valid @RequestBody ResourceRequestDTO dto,
                                                            Authentication auth) {
        ResourceRequestDTO created = service.createRequest(dto, auth.getName());
        return ResponseEntity.created(URI.create("/api/requests/" + created.getId()))
                             .body(created);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('REPORTER')")
    public List<ResourceRequestDTO> getMyRequests(Authentication auth) {
        return service.getRequestsForReporter(auth.getName());
    }

    @GetMapping("/{id:[0-9]+}")
    @PreAuthorize("hasRole('REPORTER')")
    public ResourceRequestDTO getMyRequestById(@PathVariable Long id, Authentication auth) {
        return service.getRequestByIdForReporter(id, auth.getName());
    }

    // --- Admin/Responder endpoints ---

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','RESPONDER')")
    public List<ResourceRequestDTO> getAllRequests() {
        return service.getAllRequests();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResourceRequestDTO updateRequest(@PathVariable Long id,
                                            @Valid @RequestBody ResourceRequestDTO dto) {
        dto.setId(id);
        return service.updateRequest(dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteRequest(@PathVariable Long id) {
        service.deleteRequest(id);
        return ResponseEntity.noContent().build();
    }
}
