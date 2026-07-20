package com.example.urlshortener.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

/**
 * A stored record that associates a Short Code with its Long URL, its Owner,
 * its active status, its Click Count, and its time of creation.
 *
 * <p>The {@code id} is internal only and is never used to build Short URLs
 * (Short Codes are generated randomly). {@code longUrl} is mapped to
 * {@code VARCHAR(2048)} so that URLs of up to 2048 characters can be stored
 * (Requirement 1.12); the JPA default of {@code VARCHAR(255)} would otherwise
 * reject longer addresses.
 */
@Entity
@Table(
        name = "link",
        uniqueConstraints = @UniqueConstraint(name = "uk_link_short_code", columnNames = "shortCode")
)
public class Link {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 7)
    private String shortCode;

    @Column(nullable = false, length = 2048)
    private String longUrl;

    @Column(nullable = false)
    private String ownerUsername;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private long clickCount;

    @Column(nullable = false)
    private Instant createdAt;

    protected Link() {
        // Required by JPA.
    }

    public Link(String shortCode, String longUrl, String ownerUsername) {
        this.shortCode = shortCode;
        this.longUrl = longUrl;
        this.ownerUsername = ownerUsername;
        this.active = true;
        this.clickCount = 0L;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getShortCode() {
        return shortCode;
    }

    public String getLongUrl() {
        return longUrl;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public long getClickCount() {
        return clickCount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
