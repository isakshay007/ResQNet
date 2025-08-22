package com.resqnet.service;

import com.resqnet.dto.UserDTO;
import com.resqnet.model.User;
import com.resqnet.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // --- Create a new user (REPORTER or RESPONDER only) ---
    public UserDTO createUser(UserDTO dto) {
        if (dto.getRole() == User.Role.ADMIN) {
            throw new IllegalArgumentException("You cannot create an ADMIN user via API.");
        }

        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setRole(dto.getRole());
        user.setPassword("changeme"); // default password

        User saved = userRepository.save(user);
        return mapToDTO(saved);
    }

    // --- Get all users ---
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToDTO)
                .toList();
    }

    // --- Get user by id ---
    public UserDTO getUserById(Long id) {
        return userRepository.findById(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // --- Update user (Admin only) ---
    public UserDTO updateUser(UserDTO dto) {
        User user = userRepository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setRole(dto.getRole());

        User updated = userRepository.save(user);
        return mapToDTO(updated);
    }

    // --- Helper: map entity → DTO ---
    private UserDTO mapToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
}
