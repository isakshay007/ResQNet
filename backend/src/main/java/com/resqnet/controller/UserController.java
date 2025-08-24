package com.resqnet.controller;

import com.resqnet.dto.UserCreateRequest;
import com.resqnet.dto.UserDTO;
import com.resqnet.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing Users.
 * - Reporters, Responders self-register via /api/auth/register.
 * - Admins can fetch/manage users here.
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*") // Allow frontend access
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Fetch all users (ADMIN only).
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserDTO> getAllUsers() {
        return userService.getAllUsers();
    }

    /**
     * Create a new user (Admin creates REPORTER/RESPONDER accounts).
     *
     * Reporters & Responders should normally use /api/auth/register,
     * but ADMIN can also create accounts directly here.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public UserDTO createUser(@Valid @RequestBody UserCreateRequest req) {
        return userService.createUser(req);
    }

    /**
     * Fetch a specific user by ID (ADMIN only).
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserDTO getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    /**
     * Update an existing user (ADMIN only).
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserDTO updateUser(@PathVariable Long id, @Valid @RequestBody UserDTO dto) {
        dto.setId(id);
        return userService.updateUser(dto);
    }

    /**
     * Delete a user (ADMIN only).
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }
}
