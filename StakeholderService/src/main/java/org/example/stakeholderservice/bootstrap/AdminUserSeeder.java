package org.example.stakeholderservice.bootstrap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.stakeholderservice.model.Role;
import org.example.stakeholderservice.model.User;
import org.example.stakeholderservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminUserSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap.admin.email:admin}")
    private String adminEmail;

    @Value("${app.bootstrap.admin.password:admin}")
    private String adminPassword;

    @Value("${app.bootstrap.admin.name:admin}")
    private String adminName;

    @Value("${app.bootstrap.admin.surname:admin}")
    private String adminSurname;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.existsByEmail(adminEmail)) {
            return;
        }

        User adminUser = User.builder()
                .name(adminName)
                .surname(adminSurname)
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword))
                .role(Role.ADMINISTRATOR)
                .createdAt(LocalDateTime.now())
                .build();

        try {
            userRepository.save(adminUser);
            log.info("Seeded default admin user with email='{}' and role={}.", adminEmail, Role.ADMINISTRATOR);
        } catch (DataIntegrityViolationException ex) {
            log.info("Default admin user already exists (email='{}'), skipping seeding.", adminEmail);
        }
    }
}

