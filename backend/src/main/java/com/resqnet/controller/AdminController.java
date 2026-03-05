package com.resqnet.controller;

import com.resqnet.dto.ContributionDTO;
import com.resqnet.dto.DisasterDTO;
import com.resqnet.dto.ResourceRequestDTO;
import com.resqnet.dto.UserDTO;
import com.resqnet.service.ContributionService;
import com.resqnet.service.DisasterService;
import com.resqnet.service.ResourceRequestService;
import com.resqnet.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
@Tag(name = "Admin")
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

    @Operation(summary = "Get all disasters (Admin)")
    @GetMapping("/disasters")
    @PreAuthorize("hasRole('ADMIN')")
    public List<DisasterDTO> getAllDisasters() {
        return disasterService.getAllDisasters();
    }

    @Operation(summary = "Update a disaster (Admin)")
    @PutMapping("/disasters/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public DisasterDTO updateDisaster(@PathVariable Long id, @Valid @RequestBody DisasterDTO dto) {
        dto.setId(id);
        return disasterService.updateDisaster(dto);
    }

    @Operation(summary = "Delete a disaster (Admin)")
    @DeleteMapping("/disasters/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteDisaster(@PathVariable Long id) {
        disasterService.deleteDisaster(id);
    }

    @Operation(summary = "Get all resource requests (Admin)")
    @GetMapping("/requests")
    @PreAuthorize("hasRole('ADMIN')")
    public List<ResourceRequestDTO> getAllRequests() {
        return requestService.getAllRequests();
    }

    @Operation(summary = "Update a resource request (Admin)")
    @PutMapping("/requests/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResourceRequestDTO updateRequest(@PathVariable Long id, @Valid @RequestBody ResourceRequestDTO dto) {
        dto.setId(id);
        return requestService.updateRequest(dto);
    }

    @Operation(summary = "Delete a resource request (Admin)")
    @DeleteMapping("/requests/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteRequest(@PathVariable Long id) {
        requestService.deleteRequest(id);
    }

    @Operation(summary = "Get all users (Admin)")
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserDTO> getAllUsers() {
        return userService.getAllUsers();
    }

    @Operation(summary = "Delete a user (Admin)")
    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    @Operation(summary = "Get all contributions (Admin)")
    @GetMapping("/contributions")
    @PreAuthorize("hasRole('ADMIN')")
    public List<ContributionDTO> getAllContributions() {
        return contributionService.getAllContributions(); // direct admin access
    }

    @Operation(summary = "Get contributions by resource request ID (Admin)")
    @GetMapping("/contributions/request/{requestId}")
    @PreAuthorize("hasRole('ADMIN')")
    public List<ContributionDTO> getContributionsByRequest(@PathVariable Long requestId) {
        return contributionService.getByRequest(requestId); // direct admin access
    }

    @Operation(summary = "Get contributions by responder email (Admin)")
    @GetMapping("/contributions/responder/{responderEmail}")
    @PreAuthorize("hasRole('ADMIN')")
    public List<ContributionDTO> getContributionsByResponder(@PathVariable String responderEmail) {
        return contributionService.getByResponder(responderEmail); // direct admin access
    }

    @Operation(summary = "Delete a contribution (Admin)")
    @DeleteMapping("/contributions/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteContribution(@PathVariable Long id) {
        contributionService.deleteContribution(id);
    }

    @Operation(summary = "Get admin dashboard summary with aggregated stats")
    @GetMapping("/summary")
    @PreAuthorize("hasRole('ADMIN')")
    @Cacheable("adminSummary")
    public Map<String, Object> getSummary() {
        Map<String, Object> summary = new HashMap<>();

        summary.put("totalUsers", userService.getAllUsers().size());
        summary.put("totalDisasters", disasterService.getAllDisasters().size());
        summary.put("totalRequests", requestService.getAllRequests().size());
        summary.put("totalContributions", contributionService.getAllContributions().size());

        Map<String, Long> requestStatusCounts = requestService.getAllRequests().stream()
                .collect(Collectors.groupingBy(
                        r -> r.getStatus() != null ? r.getStatus().name() : "UNKNOWN",
                        Collectors.counting()
                ));
        summary.put("requestStatusCounts", requestStatusCounts);

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
