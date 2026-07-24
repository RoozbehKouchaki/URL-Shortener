package com.example.urlshortener.service;

import com.example.urlshortener.model.DemoUser;
import com.example.urlshortener.repository.DemoUserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/** Loads demo accounts for Spring Security's {@code DaoAuthenticationProvider}. */
@Service
public class DemoUserDetailsService implements UserDetailsService {

    private final DemoUserRepository demoUserRepository;

    public DemoUserDetailsService(DemoUserRepository demoUserRepository) {
        this.demoUserRepository = demoUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        DemoUser user = demoUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Unknown user"));

        return User.withUsername(user.getUsername())
                .password(user.getPasswordHash())
                .roles(user.getRole())
                .build();
    }
}
