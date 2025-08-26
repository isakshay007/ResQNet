package com.resqnet.controller;

import com.resqnet.dto.UserCreateRequest;
import com.resqnet.dto.LoginRequest;
import com.resqnet.dto.UserDTO;
import com.resqnet.model.User;
import com.resqnet.service.UserService;
import com.resqnet.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(UserService userService, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    // Register new user (Reporter/Responder only, not Admin)
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserCreateRequest dto) {
        try {
            if (dto.getRole() == User.Role.ADMIN) {
                return ResponseEntity.badRequest().body("Admin accounts cannot be self-registered");
            }

            UserDTO saved = userService.createUser(dto);
            return ResponseEntity.ok(saved);

        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    // Login user
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest dto) {
        User user = userService.findByEmail(dto.getEmail());

        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            return ResponseEntity.status(401).body("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        return ResponseEntity.ok(Map.of(
                "token", token,
                "role", user.getRole().name()
        ));
    }
}
