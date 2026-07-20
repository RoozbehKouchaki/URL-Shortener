package com.example.urlshortener.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * A predefined Demo User account used to sign in to the service.
 *
 * <p>Only the BCrypt {@code passwordHash} is stored; plain-text passwords are
 * never persisted. The {@code username} is the natural key.
 */
@Entity
@Table(name = "demo_user")
public class DemoUser {

    @Id
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String role;

    protected DemoUser() {
        // Required by JPA.
    }

    public DemoUser(String username, String passwordHash, String role) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getRole() {
        return role;
    }
}
