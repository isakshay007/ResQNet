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
@CrossOrigin(origins = "*")
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

    @DeleteMapping("/disasters/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteDisaster(@PathVariable Long id) {
        disasterService.deleteDisaster(id);
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

    @DeleteMapping("/requests/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteRequest(@PathVariable Long id) {
        requestService.deleteRequest(id);
    }

    // ---------------- USERS ----------------
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserDTO> getAllUsers() {
        return userService.getAllUsers();
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
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

    @DeleteMapping("/contributions/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteContribution(@PathVariable Long id) {
        contributionService.deleteContribution(id);
    }

    // ---------------- DASHBOARD SUMMARY ----------------
    @GetMapping("/summary")
    @PreAuthorize("hasRole('ADMIN')")
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
