package com.resqnet.service;

import com.resqnet.dto.NotificationDTO;
import com.resqnet.dto.UserCreateRequest;
import com.resqnet.dto.UserDTO;
import com.resqnet.model.User;
import com.resqnet.producer.NotificationProducer;
import com.resqnet.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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

    @CacheEvict(value = {"users", "adminSummary"}, allEntries = true)
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
        user.setPassword(passwordEncoder.encode(req.getPassword()));

        // Save permanent location if provided
        user.setLatitude(req.getLatitude());
        user.setLongitude(req.getLongitude());

        User saved = userRepository.save(user);
        UserDTO dto = mapToDTO(saved);

        // Send Notifications
        sendUserCreationNotifications(saved);

        return dto;
    }

    @Cacheable("users")
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // --- Get user by id ---
    public UserDTO getUserById(Long id) {
        return userRepository.findById(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    @CacheEvict(value = {"users", "adminSummary"}, allEntries = true)
    @Transactional
    public UserDTO updateUser(UserDTO dto) {
        User user = userRepository.findById(dto.getId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setRole(dto.getRole());

        user.setLatitude(dto.getLatitude());
        user.setLongitude(dto.getLongitude());

        return mapToDTO(userRepository.save(user));
    }

    @CacheEvict(value = {"users", "disasters", "requests", "adminSummary"}, allEntries = true)
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        userRepository.delete(user);

        // Send Notifications
        sendUserDeletionNotifications(user);
    }

    // --- find user by email (for AuthController login) ---
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
        dto.setLatitude(user.getLatitude());
        dto.setLongitude(user.getLongitude());
        return dto;
    }

    // --- Notification helpers ---
    private void sendUserCreationNotifications(User user) {
        // Welcome to user
        NotificationDTO welcomeNotif = new NotificationDTO();
        welcomeNotif.setRecipientEmail(user.getEmail());
        welcomeNotif.setMessage("🎉 Welcome " + user.getName() + "! Your account has been created.");
        welcomeNotif.setType("WELCOME");
        welcomeNotif.setDeletable(true);
        notificationProducer.sendNotification(welcomeNotif);

        // Admin broadcast
        NotificationDTO adminNotif = new NotificationDTO();
        adminNotif.setMessage("👤 New user registered: " + user.getEmail() + " (" + user.getRole() + ")");
        adminNotif.setType("ADMIN_LOG");
        adminNotif.setDeletable(false);
        adminNotif.setAdminBroadcast(true);
        notificationProducer.sendNotification(adminNotif);
    }

    private void sendUserDeletionNotifications(User user) {
        // Admin broadcast
        NotificationDTO adminNotif = new NotificationDTO();
        adminNotif.setMessage("🗑️ User deleted: " + user.getEmail() + " (" + user.getRole() + ")");
        adminNotif.setType("ADMIN_LOG");
        adminNotif.setDeletable(false);
        adminNotif.setAdminBroadcast(true);
        notificationProducer.sendNotification(adminNotif);
    }
}
