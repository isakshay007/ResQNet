package com.resqnet.controller;

import com.resqnet.dto.UserCreateRequest;
import com.resqnet.dto.LoginRequest;
import com.resqnet.dto.UserDTO;
import com.resqnet.model.User;
import com.resqnet.service.UserService;
import com.resqnet.security.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication")
public class AuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(UserService userService, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Operation(summary = "Register a new Reporter or Responder account")
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserCreateRequest dto) {
        try {
            if (dto.getRole() == User.Role.ADMIN) {
                return ResponseEntity.badRequest().body(Map.of("error", "Admin accounts cannot be self-registered"));
            }

            UserDTO saved = userService.createUser(dto);
            return ResponseEntity.ok(Map.of(
                    "message", "User registered successfully",
                    "user", saved
            ));

        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @Operation(summary = "Login and receive a JWT token")
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest dto) {
        User user = userService.findByEmail(dto.getEmail());

        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid email or password"));
        }

        // ⚡ IMPORTANT: Pass plain role name (REPORTER / RESPONDER / ADMIN)
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        return ResponseEntity.ok(Map.of(
                "message", "Login successful",
                "token", token,
                "role", user.getRole().name(),
                "email", user.getEmail(),
                "name", user.getName()
        ));
    }

    @Operation(summary = "CORS preflight handler", hidden = true)
    @RequestMapping(value = "/**", method = RequestMethod.OPTIONS)
    public ResponseEntity<?> handleOptions() {
        return ResponseEntity.ok().build();
    }
}
