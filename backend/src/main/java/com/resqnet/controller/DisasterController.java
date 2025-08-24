package com.resqnet.controller;

import com.resqnet.dto.DisasterDTO;
import com.resqnet.service.DisasterService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing Disasters.
 * Reporters can create disasters, and the frontend can fetch them
 * for global map display.
 */
@RestController
@RequestMapping("/api/disasters")
@CrossOrigin(origins = "*") // Allow frontend access
public class DisasterController {

    private final DisasterService disasterService;

    public DisasterController(DisasterService disasterService) {
        this.disasterService = disasterService;
    }

    /**
     * Create a new disaster report (Reporter action).
     *
     * Only REPORTERs are allowed to create.
     *  We no longer trust `reporterEmail` from the DTO.
     *  Instead, we always use the authenticated user (auth.getName()).
     */
    @PostMapping
    @PreAuthorize("hasRole('REPORTER')")
    public DisasterDTO createDisaster(@Valid @RequestBody DisasterDTO dto, Authentication auth) {
        return disasterService.createDisaster(dto, auth.getName());
    }

    /**
     * Fetch all disasters (used for global map display).
     *
     * Open to all authenticated users.
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<DisasterDTO> getAllDisasters() {
        return disasterService.getAllDisasters();
    }

    /**
     * Fetch a specific disaster by ID.
     *
     * Open to all authenticated users.
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public DisasterDTO getDisasterById(@PathVariable Long id) {
        return disasterService.getDisasterById(id);
    }
}
