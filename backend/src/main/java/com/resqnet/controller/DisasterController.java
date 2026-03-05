package com.resqnet.controller;

import com.resqnet.dto.DisasterDTO;
import com.resqnet.service.DisasterService;
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
@RequestMapping("/api/disasters")
@CrossOrigin(origins = "*")
@Tag(name = "Disasters")
public class DisasterController {

    private final DisasterService disasterService;

    public DisasterController(DisasterService disasterService) {
        this.disasterService = disasterService;
    }

    @Operation(summary = "Report a new disaster (Reporter only)")
    @PostMapping
    @PreAuthorize("hasRole('REPORTER')")
    public ResponseEntity<DisasterDTO> createDisaster(@Valid @RequestBody DisasterDTO dto,
                                                      Authentication auth) {
        DisasterDTO created = disasterService.createDisaster(dto, auth.getName());
        return ResponseEntity.created(URI.create("/api/disasters/" + created.getId()))
                             .body(created);
    }

    @Operation(summary = "Get all disasters")
    @GetMapping
    @PreAuthorize("hasAnyRole('REPORTER','RESPONDER','ADMIN')")
    public List<DisasterDTO> getAllDisasters(Authentication auth) {
        // later: can add role-based filtering if needed
        return disasterService.getAllDisasters();
    }

    @Operation(summary = "Get a disaster by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('REPORTER','RESPONDER','ADMIN')")
    public DisasterDTO getDisasterById(@PathVariable Long id, Authentication auth) {
        return disasterService.getDisasterById(id);
    }

    @Operation(summary = "Update a disaster (Admin only)")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public DisasterDTO updateDisaster(@PathVariable Long id,
                                      @Valid @RequestBody DisasterDTO dto) {
        dto.setId(id);
        return disasterService.updateDisaster(dto);
    }

    @Operation(summary = "Delete a disaster (Admin only)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteDisaster(@PathVariable Long id) {
        disasterService.deleteDisaster(id);
        return ResponseEntity.noContent().build();
    }
}
