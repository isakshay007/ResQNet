package com.resqnet.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.resqnet.model.User;

import java.time.LocalDateTime;

public class UserDTO {

    @JsonProperty(access = Access.READ_ONLY) // id is only returned, never set by frontend
    private Long id;

    private String name;

    private String email;

    private User.Role role;

    @JsonProperty(access = Access.READ_ONLY) // system-managed
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDateTime createdAt;

    // --- Getters & Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public User.Role getRole() { return role; }
    public void setRole(User.Role role) { this.role = role; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
