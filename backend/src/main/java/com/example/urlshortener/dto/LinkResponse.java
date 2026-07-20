package com.example.urlshortener.dto;

import com.example.urlshortener.model.Link;

import java.time.Instant;

/**
 * The client-facing view of a Link. Deliberately excludes the internal id and
 * owner so neither is ever exposed over the API.
 */
public record LinkResponse(
        String shortCode,
        String shortUrl,
        String longUrl,
        boolean active,
        long clickCount,
        Instant createdAt
) {

    /**
     * Build a response from an entity, joining {@code baseAddress} with the
     * Short Code to form the Short URL.
     */
    public static LinkResponse from(Link link, String baseAddress) {
        String trimmedBase = baseAddress.endsWith("/")
                ? baseAddress.substring(0, baseAddress.length() - 1)
                : baseAddress;
        String shortUrl = trimmedBase + "/" + link.getShortCode();

        return new LinkResponse(
                link.getShortCode(),
                shortUrl,
                link.getLongUrl(),
                link.isActive(),
                link.getClickCount(),
                link.getCreatedAt());
    }
}
