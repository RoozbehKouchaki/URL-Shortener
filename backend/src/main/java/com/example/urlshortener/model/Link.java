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
 * {@code longUrl} is {@code VARCHAR(2048)} because the JPA default of 255 would
 * reject longer addresses. The {@code id} is internal and never used to build
 * Short URLs.
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
