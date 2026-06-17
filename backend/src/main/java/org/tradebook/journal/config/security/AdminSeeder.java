package org.tradebook.journal.config.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.tradebook.journal.features.auth.entity.User;
import org.tradebook.journal.features.auth.enums.Role;
import org.tradebook.journal.features.auth.repository.UserRepository;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        String adminEmail = "admin@tradebook.com";
        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            log.info("Bootstrapping default admin user: {}", adminEmail);
            User admin = User.builder()
                    .email(adminEmail)
                    .passwordHash(passwordEncoder.encode("Admin@123"))
                    .role(Role.ADMIN)
                    .currency("USD")
                    .build();
            userRepository.save(admin);
            log.info("Default admin user successfully bootstrapped.");
        } else {
            log.info("Admin user {} already exists in the database. Skipping seeding.", adminEmail);
        }
    }
}
