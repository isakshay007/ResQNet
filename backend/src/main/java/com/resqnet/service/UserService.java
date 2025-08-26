package com.resqnet.service;

import com.resqnet.dto.NotificationDTO;
import com.resqnet.dto.UserCreateRequest;
import com.resqnet.dto.UserDTO;
import com.resqnet.model.User;
import com.resqnet.producer.NotificationProducer;
import com.resqnet.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationProducer notificationProducer;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       NotificationProducer notificationProducer) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.notificationProducer = notificationProducer;
    }

    // --- Create a new user (REPORTER or RESPONDER only) ---
    @Transactional
    public UserDTO createUser(UserCreateRequest req) {
        if (req.getRole() == User.Role.ADMIN) {
            throw new IllegalArgumentException("You cannot create an ADMIN user via API.");
        }

        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User();
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        user.setRole(req.getRole());
        user.setPassword(passwordEncoder.encode(req.getPassword())); // secure hash

        User saved = userRepository.save(user);
        UserDTO dto = mapToDTO(saved);

        // 🔹 Send Notifications
        sendUserCreationNotifications(saved);

        return dto;
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
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    // --- Update user (Admin only, no password updates here) ---
    @Transactional
    public UserDTO updateUser(UserDTO dto) {
        User user = userRepository.findById(dto.getId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setRole(dto.getRole());

        return mapToDTO(userRepository.save(user));
    }

    // --- Delete user (Admin only) ---
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        userRepository.delete(user);

        //  Send Notifications
        sendUserDeletionNotifications(user);
    }

    // --- 🔹 New: find user by email (for AuthController login) ---
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
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

    // --- Notification helpers ---
    private void sendUserCreationNotifications(User user) {
        NotificationDTO welcomeNotif = new NotificationDTO();
        welcomeNotif.setRecipientEmail(user.getEmail());
        welcomeNotif.setMessage(" Welcome " + user.getName() + "! Your account has been created 🎉");
        welcomeNotif.setType("WELCOME");
        welcomeNotif.setDeletable(true);
        notificationProducer.sendNotification(welcomeNotif);

        userRepository.findAll().stream()
                .filter(u -> u.getRole() == User.Role.ADMIN)
                .forEach(admin -> {
                    NotificationDTO adminNotif = new NotificationDTO();
                    adminNotif.setRecipientEmail(admin.getEmail());
                    adminNotif.setMessage(" New user registered: "
                            + user.getEmail() + " (" + user.getRole() + ")");
                    adminNotif.setType("ADMIN_LOG");
                    adminNotif.setDeletable(false);
                    notificationProducer.sendNotification(adminNotif);
                });
    }

    private void sendUserDeletionNotifications(User user) {
        userRepository.findAll().stream()
                .filter(u -> u.getRole() == User.Role.ADMIN)
                .forEach(admin -> {
                    NotificationDTO adminNotif = new NotificationDTO();
                    adminNotif.setRecipientEmail(admin.getEmail());
                    adminNotif.setMessage(" User deleted: "
                            + user.getEmail() + " (" + user.getRole() + ")");
                    adminNotif.setType("ADMIN_LOG");
                    adminNotif.setDeletable(false);
                    notificationProducer.sendNotification(adminNotif);
                });
    }
}
