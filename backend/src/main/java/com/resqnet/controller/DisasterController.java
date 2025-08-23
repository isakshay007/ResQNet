package com.resqnet.controller;

import com.resqnet.dto.DisasterDTO;
import com.resqnet.service.DisasterService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
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
     */
    @PostMapping
    @PreAuthorize("hasRole('REPORTER')")
    public DisasterDTO createDisaster(@Valid @RequestBody DisasterDTO dto) {
        return disasterService.createDisaster(dto);
    }

    /**
     * Fetch all disasters (used for global map display).
     *
     * Open to all authenticated users.
     */
    @GetMapping
    public List<DisasterDTO> getAllDisasters() {
        return disasterService.getAllDisasters();
    }

    /**
     * Fetch a specific disaster by ID.
     *
     * Open to all authenticated users.
     */
    @GetMapping("/{id}")
    public DisasterDTO getDisasterById(@PathVariable Long id) {
        return disasterService.getDisasterById(id);
    }
}
