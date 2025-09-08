package com.resqnet.config;

import com.resqnet.model.User;
import com.resqnet.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.default.password}") 
    private String defaultPassword;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void seedAdmin() {
        final String adminEmail = "admin@resqnet.com";

        userRepository.findByEmail(adminEmail).ifPresentOrElse(
            user -> {
                // If user exists but not admin, upgrade role
                if (user.getRole() != User.Role.ADMIN) {
                    user.setRole(User.Role.ADMIN);
                    userRepository.save(user);
                    System.out.println("⚠ Existing user with " + adminEmail + " upgraded to ADMIN.");
                } else {
                    System.out.println("ℹ Admin already exists. Seeder skipped.");
                }
            },
            () -> {
                // Create new admin
                User admin = new User();
                admin.setName("System Admin");
                admin.setEmail(adminEmail);
                admin.setPassword(passwordEncoder.encode(defaultPassword)); // hash it
                admin.setRole(User.Role.ADMIN);

                userRepository.save(admin);

                System.out.println(" Default admin account created: " + adminEmail);
                System.out.println(" Please change the default password immediately after first login.");
            }
        );
    }
}
