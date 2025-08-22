package com.resqnet.controller;

import com.resqnet.dto.DisasterDTO;
import com.resqnet.service.DisasterService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing Disasters.
 * Reporters can create disasters, and the frontend can fetch them
 * for global map display.
 */
@RestController
@RequestMapping("/api/disasters")
@CrossOrigin(origins = "*") // ✅ Allow frontend access
public class DisasterController {

    private final DisasterService disasterService;

    public DisasterController(DisasterService disasterService) {
        this.disasterService = disasterService;
    }

    /**
     * Create a new disaster report (Reporter action).
     *
     * @param dto DisasterDTO with type, severity, description, lat/lng
     * @return saved DisasterDTO
     */
    @PostMapping
    public DisasterDTO createDisaster(@Valid @RequestBody DisasterDTO dto) {
        return disasterService.createDisaster(dto);
    }

    /**
     * Fetch all disasters (used for global map display).
     *
     * @return list of DisasterDTO
     */
    @GetMapping
    public List<DisasterDTO> getAllDisasters() {
        return disasterService.getAllDisasters();
    }

    /**
     * Fetch a specific disaster by ID.
     *
     * @param id disaster ID
     * @return DisasterDTO
     */
    @GetMapping("/{id}")
    public DisasterDTO getDisasterById(@PathVariable Long id) {
        return disasterService.getDisasterById(id);
    }
}
