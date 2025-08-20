package com.resqnet.controller;

import com.resqnet.model.Disaster;
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
    public Disaster createDisaster(@RequestBody Disaster disaster) {
        return disasterService.createDisaster(disaster);
    }

    @GetMapping
    public List<Disaster> getAllDisasters() {
        return disasterService.getAllDisasters();
    }

    @GetMapping("/{id}")
    public Disaster getDisasterById(@PathVariable Long id) {
        return disasterService.getDisasterById(id);
    }
}
