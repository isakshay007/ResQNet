package com.resqnet.controller;

import com.resqnet.dto.DisasterDTO;
import com.resqnet.dto.ResourceRequestDTO;
import com.resqnet.dto.UserDTO;
import com.resqnet.service.DisasterService;
import com.resqnet.service.ResourceRequestService;
import com.resqnet.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*") // Allow frontend access
public class AdminController {

    private final DisasterService disasterService;
    private final ResourceRequestService requestService;
    private final UserService userService;

    public AdminController(
            DisasterService disasterService,
            ResourceRequestService requestService,
            UserService userService
    ) {
        this.disasterService = disasterService;
        this.requestService = requestService;
        this.userService = userService;
    }

    // ---------------- DISASTERS ----------------

    @GetMapping("/disasters")
    public List<DisasterDTO> getAllDisasters() {
        return disasterService.getAllDisasters();
    }

    @PutMapping("/disasters/{id}")
    public DisasterDTO updateDisaster(@PathVariable Long id, @Valid @RequestBody DisasterDTO dto) {
        dto.setId(id);
        return disasterService.updateDisaster(dto);
    }

    // ---------------- REQUESTS ----------------

    @GetMapping("/requests")
    public List<ResourceRequestDTO> getAllRequests() {
        return requestService.getAllRequests();
    }

    @PutMapping("/requests/{id}")
    public ResourceRequestDTO updateRequest(@PathVariable Long id, @Valid @RequestBody ResourceRequestDTO dto) {
        dto.setId(id);
        return requestService.updateRequest(dto);
    }

    // ---------------- USERS ----------------

    @GetMapping("/users")
    public List<UserDTO> getAllUsers() {
        return userService.getAllUsers();
    }
}
