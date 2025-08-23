package com.resqnet.controller;

import com.resqnet.dto.UserCreateRequest;
import com.resqnet.dto.LoginRequest;
import com.resqnet.model.User;
import com.resqnet.repository.UserRepository;
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

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    // Register new user (Reporter/Responder only, not Admin)
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserCreateRequest dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            return ResponseEntity.badRequest().body("Email already exists");
        }

        //  Prevent self-registration as ADMIN
        if (dto.getRole() == User.Role.ADMIN) {
            return ResponseEntity.badRequest().body("Admin accounts cannot be self-registered");
        }

        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword())); // hash password
        user.setRole(dto.getRole());

        userRepository.save(user);
        return ResponseEntity.ok("User registered successfully");
    }

    // Login user (secure: always generic error messages)
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest dto) {
        User user = userRepository.findByEmail(dto.getEmail()).orElse(null);

        // Never leak if user doesn't exist → always generic error
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
