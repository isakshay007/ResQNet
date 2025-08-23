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

    @Value("${admin.default.password}") // inject from application.properties / .env
    private String defaultPassword;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
            admin.setPassword(passwordEncoder.encode(defaultPassword)); //  hashed
            admin.setRole(User.Role.ADMIN);

            userRepository.save(admin);

            System.out.println(" Default admin account created: " + adminEmail);
            System.out.println(" Please change the default password immediately after first login.");
        } else {
            System.out.println("ℹ Admin already exists. Seeder skipped.");
        }
    }
}
