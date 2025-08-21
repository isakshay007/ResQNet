package com.resqnet.controller;

import com.resqnet.dto.DisasterDTO;
import com.resqnet.service.DisasterService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/disasters")
public class DisasterController {

    private final DisasterService disasterService;

    public DisasterController(DisasterService disasterService) {
        this.disasterService = disasterService;
    }

    @PostMapping
    public DisasterDTO createDisaster(@RequestBody DisasterDTO dto) {
        return disasterService.createDisaster(dto);
    }

    @GetMapping
    public List<DisasterDTO> getAllDisasters() {
        return disasterService.getAllDisasters();
    }

    @GetMapping("/{id}")
    public DisasterDTO getDisasterById(@PathVariable Long id) {
        return disasterService.getDisasterById(id);
    }
}
