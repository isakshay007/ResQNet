package com.resqnet.controller;

import com.resqnet.dto.UserDTO;
import com.resqnet.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing Users.
 * - Reporters, Responders, and Admins are created and fetched via this controller.
 * - Password handling will be added later during Security integration.
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*") // ✅ Allow frontend access
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Fetch all users (Admin only in the future).
     *
     * @return list of UserDTO
     */
    @GetMapping
    public List<UserDTO> getAllUsers() {
        return userService.getAllUsers();
    }

    /**
     * Create a new user (Reporter / Responder via signup).
     *
     * @param dto UserDTO containing user details
     * @return saved UserDTO
     */
    @PostMapping
    public UserDTO createUser(@Valid @RequestBody UserDTO dto) {
        return userService.createUser(dto);
    }

    /**
     * Fetch a specific user by ID.
     *
     * @param id user ID
     * @return UserDTO
     */
    @GetMapping("/{id}")
    public UserDTO getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }
}
