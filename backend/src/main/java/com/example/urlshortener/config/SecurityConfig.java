package com.example.urlshortener.config;

import com.example.urlshortener.security.RestAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * HTTP Basic security configuration.
 *
 * <ul>
 *   <li>{@code permitAll} on the public redirect path
 *       {@code GET /{shortCode:[A-Za-z0-9]{7}}} and the error path.</li>
 *   <li>{@code authenticated} on {@code /api/**}.</li>
 *   <li>Stateless session policy: identity is re-established from the
 *       credentials on every request (Requirement 6.5).</li>
 *   <li>A custom {@link RestAuthenticationEntryPoint} writes the 401
 *       {@code ErrorResponse} JSON and omits {@code WWW-Authenticate}.</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Passwords are verified against BCrypt hashes, never plain text.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   RestAuthenticationEntryPoint authenticationEntryPoint)
            throws Exception {
        http
                // Stateless REST API + Basic auth: no CSRF tokens or server-side sessions.
                .csrf(csrf -> csrf.disable())
                // Allow the H2 console (dev only) to render inside its frame.
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .authorizeHttpRequests(auth -> auth
                        // Public redirect path: exactly seven base62 characters.
                        .requestMatchers(HttpMethod.GET, "/{shortCode:[A-Za-z0-9]{7}}").permitAll()
                        // Error rendering must never require authentication.
                        .requestMatchers("/error").permitAll()
                        // H2 console for local development.
                        .requestMatchers("/h2-console/**").permitAll()
                        // All management endpoints require valid credentials.
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().authenticated())
                .httpBasic(basic -> basic.authenticationEntryPoint(authenticationEntryPoint))
                .exceptionHandling(ex -> ex.authenticationEntryPoint(authenticationEntryPoint))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}
