package com.financial.config;

import com.financial.entity.User;
import com.financial.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Data initializer to ensure default users have correct passwords.
 * Runs on application startup (excluded from test profile).
 */
@Component
@Profile("!test")
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructor with lazy password encoder to avoid circular dependencies.
     *
     * @param userRepository the user repository
     * @param passwordEncoder the password encoder (lazy loaded)
     */
    public DataInitializer(UserRepository userRepository, @Lazy PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        log.info("Checking and updating default user passwords...");
        
        // Update admin user password
        userRepository.findByUsername("admin").ifPresent(admin -> {
            String encodedPassword = passwordEncoder.encode("admin123");
            admin.setPassword(encodedPassword);
            userRepository.save(admin);
            log.info("Admin user password updated successfully");
        });

        // Update regular user password
        userRepository.findByUsername("user").ifPresent(user -> {
            String encodedPassword = passwordEncoder.encode("user123");
            user.setPassword(encodedPassword);
            userRepository.save(user);
            log.info("Regular user password updated successfully");
        });
        
        log.info("Default user passwords initialization complete");
    }
}

