package com.resqnet.dto;

import com.resqnet.model.User;
import java.time.LocalDateTime;

public class UserDTO {
    private Long id;
    private String name;
    private String email;
    private User.Role role;
    private LocalDateTime createdAt;   //  new field

    // --- Getters & Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public User.Role getRole() { return role; }
    public void setRole(User.Role role) { this.role = role; }

    public LocalDateTime getCreatedAt() { return createdAt; }   //  getter
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }  //  setter
}
