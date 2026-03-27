package com.resqnet.config;

import com.resqnet.model.User;
import com.resqnet.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.seed.default-admin", havingValue = "true")
public class DataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

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
                if (user.getRole() != User.Role.ADMIN) {
                    log.warn("User {} already exists but is not ADMIN. Seeder will not auto-elevate privileges.", adminEmail);
                } else {
                    log.info("Admin already exists. Seeder skipped.");
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

                log.warn("Default admin account created for {}. Change the password immediately.", adminEmail);
            }
        );
    }
}
