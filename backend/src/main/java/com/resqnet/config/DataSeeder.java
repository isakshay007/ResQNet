package com.resqnet.config;

import com.resqnet.model.User;
import com.resqnet.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder {

    private final UserRepository userRepository;

    public DataSeeder(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostConstruct
    public void seedAdmin() {
        String adminEmail = "admin@resqnet.com";

        boolean adminExists = userRepository.findAll().stream()
                .anyMatch(u -> u.getRole() == User.Role.ADMIN);

        if (!adminExists) {
            User admin = new User();
            admin.setName("System Admin");
            admin.setEmail(adminEmail);
            admin.setPassword("admin123"); // plaintext for now, later use BCrypt
            admin.setRole(User.Role.ADMIN);

            userRepository.save(admin);
            System.out.println(" Default admin created: " + adminEmail + " / admin123");
        } else {
            System.out.println("ℹAdmin already exists. Seeder skipped.");
        }
    }
}
