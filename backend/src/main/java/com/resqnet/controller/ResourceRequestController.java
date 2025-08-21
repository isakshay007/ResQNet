package com.resqnet.controller;

import com.resqnet.dto.ResourceRequestDTO;
import com.resqnet.service.ResourceRequestService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/requests")
public class ResourceRequestController {

    private final ResourceRequestService service;

    public ResourceRequestController(ResourceRequestService service) {
        this.service = service;
    }

    @PostMapping
    public ResourceRequestDTO createRequest(@RequestBody ResourceRequestDTO dto) {
        return service.createRequest(dto);
    }

    @GetMapping
    public List<ResourceRequestDTO> getAllRequests() {
        return service.getAllRequests();
    }

    @GetMapping("/{id}")
    public ResourceRequestDTO getRequestById(@PathVariable Long id) {
        return service.getRequestById(id);
    }
}
