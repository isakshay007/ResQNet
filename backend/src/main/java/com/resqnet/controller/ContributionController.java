package com.resqnet.controller;

import com.resqnet.dto.ContributionDTO;
import com.resqnet.service.ContributionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contributions")
public class ContributionController {

    private final ContributionService service;

    public ContributionController(ContributionService service) {
        this.service = service;
    }

    @PostMapping
    public ContributionDTO createContribution(@RequestBody ContributionDTO dto) {
        return service.createContribution(dto);
    }

    @GetMapping
    public List<ContributionDTO> getAllContributions() {
        return service.getAllContributions();
    }

    @GetMapping("/request/{requestId}")
    public List<ContributionDTO> getByRequest(@PathVariable Long requestId) {
        return service.getByRequest(requestId);
    }

    // ✅ Now email-based instead of id
    @GetMapping("/responder/{responderEmail}")
    public List<ContributionDTO> getByResponder(@PathVariable String responderEmail) {
        return service.getByResponder(responderEmail);
    }
}
