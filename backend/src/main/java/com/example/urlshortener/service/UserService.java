package com.example.urlshortener.service;

import com.example.urlshortener.exception.UsernameTakenException;
import com.example.urlshortener.model.DemoUser;
import com.example.urlshortener.repository.DemoUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private static final String DEFAULT_ROLE = "USER";

    private final DemoUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(DemoUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * The username is the primary key, so a concurrent duplicate is rejected by the
     * database even if it slips past this check.
     */
    public void register(String username, String rawPassword) {
        if (userRepository.existsByUsername(username)) {
            throw new UsernameTakenException("Username is already taken.");
        }
        userRepository.save(new DemoUser(username, passwordEncoder.encode(rawPassword), DEFAULT_ROLE));
    }
}
