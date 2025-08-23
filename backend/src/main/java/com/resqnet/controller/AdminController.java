package com.resqnet.controller;

import com.resqnet.dto.ContributionDTO;
import com.resqnet.dto.DisasterDTO;
import com.resqnet.dto.ResourceRequestDTO;
import com.resqnet.dto.UserDTO;
import com.resqnet.service.ContributionService;
import com.resqnet.service.DisasterService;
import com.resqnet.service.ResourceRequestService;
import com.resqnet.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*") // Allow frontend access
public class AdminController {

    private final DisasterService disasterService;
    private final ResourceRequestService requestService;
    private final UserService userService;
    private final ContributionService contributionService;

    public AdminController(
            DisasterService disasterService,
            ResourceRequestService requestService,
            UserService userService,
            ContributionService contributionService
    ) {
        this.disasterService = disasterService;
        this.requestService = requestService;
        this.userService = userService;
        this.contributionService = contributionService;
    }

    // ---------------- DISASTERS ----------------
    @GetMapping("/disasters")
    @PreAuthorize("hasRole('ADMIN')")
    public List<DisasterDTO> getAllDisasters() {
        return disasterService.getAllDisasters();
    }

    @PutMapping("/disasters/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public DisasterDTO updateDisaster(@PathVariable Long id, @Valid @RequestBody DisasterDTO dto) {
        dto.setId(id);
        return disasterService.updateDisaster(dto);
    }

    // ---------------- REQUESTS ----------------
    @GetMapping("/requests")
    @PreAuthorize("hasRole('ADMIN')")
    public List<ResourceRequestDTO> getAllRequests() {
        return requestService.getAllRequests();
    }

    @PutMapping("/requests/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResourceRequestDTO updateRequest(@PathVariable Long id, @Valid @RequestBody ResourceRequestDTO dto) {
        dto.setId(id);
        return requestService.updateRequest(dto);
    }

    // ---------------- USERS ----------------
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserDTO> getAllUsers() {
        return userService.getAllUsers();
    }

    // ---------------- CONTRIBUTIONS ----------------
    @GetMapping("/contributions")
    @PreAuthorize("hasRole('ADMIN')")
    public List<ContributionDTO> getAllContributions() {
        return contributionService.getAllContributions();
    }

    @GetMapping("/contributions/request/{requestId}")
    @PreAuthorize("hasRole('ADMIN')")
    public List<ContributionDTO> getContributionsByRequest(@PathVariable Long requestId) {
        return contributionService.getByRequest(requestId);
    }

    @GetMapping("/contributions/responder/{responderEmail}")
    @PreAuthorize("hasRole('ADMIN')")
    public List<ContributionDTO> getContributionsByResponder(@PathVariable String responderEmail) {
        return contributionService.getByResponder(responderEmail);
    }

    // ---------------- DASHBOARD SUMMARY ----------------
    @GetMapping("/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> getSummary() {
        Map<String, Object> summary = new HashMap<>();

        // Totals
        summary.put("totalUsers", userService.getAllUsers().size());
        summary.put("totalDisasters", disasterService.getAllDisasters().size());
        summary.put("totalRequests", requestService.getAllRequests().size());
        summary.put("totalContributions", contributionService.getAllContributions().size());

        // Status-based breakdown of requests
        Map<String, Long> requestStatusCounts = requestService.getAllRequests().stream()
                .collect(Collectors.groupingBy(
                        r -> r.getStatus() != null ? r.getStatus().name() : "UNKNOWN",
                        Collectors.counting()
                ));
        summary.put("requestStatusCounts", requestStatusCounts);

        // Role-based breakdown of users (exclude ADMIN)
        Map<String, Long> userRoleCounts = userService.getAllUsers().stream()
                .filter(u -> u.getRole() != null && !u.getRole().name().equals("ADMIN"))
                .collect(Collectors.groupingBy(
                        u -> u.getRole().name(),
                        Collectors.counting()
                ));
        summary.put("userRoleCounts", userRoleCounts);

        return summary;
    }
}
