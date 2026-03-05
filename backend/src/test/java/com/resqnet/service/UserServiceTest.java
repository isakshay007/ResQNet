package com.resqnet.service;

import com.resqnet.dto.UserCreateRequest;
import com.resqnet.dto.UserDTO;
import com.resqnet.model.User;
import com.resqnet.producer.NotificationProducer;
import com.resqnet.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private NotificationProducer notificationProducer;

    @InjectMocks private UserService userService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setId(1L);
        sampleUser.setName("John Doe");
        sampleUser.setEmail("john@example.com");
        sampleUser.setRole(User.Role.REPORTER);
        sampleUser.setPassword("hashedpassword");
        sampleUser.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void createUser_withValidReporter_returnsUserDTO() {
        UserCreateRequest req = new UserCreateRequest();
        req.setName("John Doe");
        req.setEmail("john@example.com");
        req.setPassword("password123");
        req.setRole(User.Role.REPORTER);

        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashedpassword");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        UserDTO result = userService.createUser(req);

        assertNotNull(result);
        assertEquals("john@example.com", result.getEmail());
        assertEquals("John Doe", result.getName());
        assertEquals(User.Role.REPORTER, result.getRole());
        verify(userRepository).save(any(User.class));
        verify(notificationProducer, times(2)).sendNotification(any());
    }

    @Test
    void createUser_withDuplicateEmail_throwsIllegalArgument() {
        UserCreateRequest req = new UserCreateRequest();
        req.setName("John Doe");
        req.setEmail("john@example.com");
        req.setPassword("password123");
        req.setRole(User.Role.REPORTER);

        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.createUser(req));
        assertEquals("Email already exists", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_withAdminRole_throwsIllegalArgument() {
        UserCreateRequest req = new UserCreateRequest();
        req.setRole(User.Role.ADMIN);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.createUser(req));
        assertTrue(ex.getMessage().contains("ADMIN"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void getAllUsers_returnsMappedDTOList() {
        when(userRepository.findAll()).thenReturn(List.of(sampleUser));

        List<UserDTO> result = userService.getAllUsers();

        assertEquals(1, result.size());
        assertEquals("john@example.com", result.get(0).getEmail());
        assertEquals(User.Role.REPORTER, result.get(0).getRole());
    }

    @Test
    void getUserById_whenNotFound_throwsEntityNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> userService.getUserById(99L));
    }

    @Test
    void deleteUser_withExistingId_deletesAndNotifies() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));

        userService.deleteUser(1L);

        verify(userRepository).delete(sampleUser);
        verify(notificationProducer).sendNotification(any());
    }
}
