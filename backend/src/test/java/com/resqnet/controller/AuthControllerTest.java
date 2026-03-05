package com.resqnet.controller;

import com.resqnet.dto.UserCreateRequest;
import com.resqnet.dto.UserDTO;
import com.resqnet.model.User;
import com.resqnet.security.JwtFilter;
import com.resqnet.security.JwtUtil;
import com.resqnet.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private UserService userService;
    @MockitoBean private PasswordEncoder passwordEncoder;
    @MockitoBean private JwtUtil jwtUtil;
    @MockitoBean private JwtFilter jwtFilter;

    @Test
    void register_withValidData_returns200() throws Exception {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setName("John");
        userDTO.setEmail("john@example.com");
        userDTO.setRole(User.Role.REPORTER);

        when(userService.createUser(any(UserCreateRequest.class))).thenReturn(userDTO);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "John",
                                    "email": "john@example.com",
                                    "password": "password123",
                                    "role": "REPORTER"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.user.email").value("john@example.com"));
    }

    @Test
    void register_withDuplicateEmail_returns400() throws Exception {
        when(userService.createUser(any(UserCreateRequest.class)))
                .thenThrow(new IllegalArgumentException("Email already exists"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "John",
                                    "email": "john@example.com",
                                    "password": "password123",
                                    "role": "REPORTER"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Email already exists"));
    }

    @Test
    void login_withValidCredentials_returnsJwtToken() throws Exception {
        User user = new User();
        user.setEmail("john@example.com");
        user.setPassword("hashedpw");
        user.setName("John");
        user.setRole(User.Role.REPORTER);

        when(userService.findByEmail("john@example.com")).thenReturn(user);
        when(passwordEncoder.matches("password123", "hashedpw")).thenReturn(true);
        when(jwtUtil.generateToken("john@example.com", "REPORTER")).thenReturn("jwt-token-123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "john@example.com",
                                    "password": "password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-123"))
                .andExpect(jsonPath("$.role").value("REPORTER"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void login_withInvalidCredentials_returns401() throws Exception {
        when(userService.findByEmail("john@example.com")).thenReturn(null);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "john@example.com",
                                    "password": "wrongpassword"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid email or password"));
    }
}
