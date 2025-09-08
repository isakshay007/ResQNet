package com.resqnet.controller;

import com.resqnet.dto.DisasterDTO;
import com.resqnet.service.DisasterService;
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
public class DisasterController {

    private final DisasterService disasterService;

    public DisasterController(DisasterService disasterService) {
        this.disasterService = disasterService;
    }

    // --- Reporter actions ---
    @PostMapping
    @PreAuthorize("hasRole('REPORTER')")
    public ResponseEntity<DisasterDTO> createDisaster(@Valid @RequestBody DisasterDTO dto,
                                                      Authentication auth) {
        DisasterDTO created = disasterService.createDisaster(dto, auth.getName());
        return ResponseEntity.created(URI.create("/api/disasters/" + created.getId()))
                             .body(created);
    }

    // --- Common (Reporter, Responder, Admin) ---
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<DisasterDTO> getAllDisasters() {
        return disasterService.getAllDisasters();
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public DisasterDTO getDisasterById(@PathVariable Long id) {
        return disasterService.getDisasterById(id);
    }

    // --- Admin actions ---
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public DisasterDTO updateDisaster(@PathVariable Long id,
                                      @Valid @RequestBody DisasterDTO dto) {
        dto.setId(id);
        return disasterService.updateDisaster(dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteDisaster(@PathVariable Long id) {
        disasterService.deleteDisaster(id);
        return ResponseEntity.noContent().build();
    }
}
