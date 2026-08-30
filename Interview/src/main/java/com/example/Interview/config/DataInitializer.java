package com.example.Interview.config;

import com.example.Interview.auth.Entity.User;
import com.example.Interview.auth.Repository.UserRepository;
import com.example.Interview.auth.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Seeds the first ADMIN account on startup if none exists.
 * Credentials: admin@interviewiq.com / Admin@123456
 * Change the password immediately after first login.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String ADMIN_EMAIL    = "admin@interviewiq.com";
    private static final String ADMIN_PASSWORD = "Admin@123456";
    private static final String ADMIN_NAME     = "Platform Admin";

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.existsByEmail(ADMIN_EMAIL)) {
            return; // Admin already seeded
        }

        User admin = User.builder()
                .fullName(ADMIN_NAME)
                .email(ADMIN_EMAIL)
                .passwordHash(passwordEncoder.encode(ADMIN_PASSWORD))
                .role(Role.ADMIN)
                .build();

        userRepository.save(admin);
        log.info("=== ADMIN SEEDED: {} | default password: {} ===", ADMIN_EMAIL, ADMIN_PASSWORD);
    }
}
