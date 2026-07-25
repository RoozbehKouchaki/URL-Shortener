package com.example.urlshortener.config;

import com.example.urlshortener.model.DemoUser;
import com.example.urlshortener.repository.DemoUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Seeds the demo accounts on startup, creating each only if absent so a restart
 * neither wipes nor duplicates them. Kept alongside sign-up so the app has a
 * working login on first launch. A real deployment would not ship this class.
 */
@Component
public class DemoUserSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoUserSeeder.class);

    /** The fixed demo accounts: username -> plain password (hashed before storage). */
    private static final Map<String, String> DEMO_ACCOUNTS = Map.of(
            "alice", "alice-password",
            "bob", "bob-password"
    );

    private static final String DEFAULT_ROLE = "USER";

    private final DemoUserRepository demoUserRepository;
    private final PasswordEncoder passwordEncoder;

    public DemoUserSeeder(DemoUserRepository demoUserRepository, PasswordEncoder passwordEncoder) {
        this.demoUserRepository = demoUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        DEMO_ACCOUNTS.forEach((username, rawPassword) -> {
            if (demoUserRepository.existsByUsername(username)) {
                log.info("Demo user '{}' already present; leaving unchanged.", username);
                return;
            }
            demoUserRepository.save(
                    new DemoUser(username, passwordEncoder.encode(rawPassword), DEFAULT_ROLE));
            log.info("Seeded demo user '{}'.", username);
        });
    }
}
