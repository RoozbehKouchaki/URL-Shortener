package com.example.urlshortener.dto;

import com.example.urlshortener.model.Link;

import java.time.Instant;

/** Excludes the internal id and owner so neither is exposed over the API. */
public record LinkResponse(
        String shortCode,
        String shortUrl,
        String longUrl,
        boolean active,
        long clickCount,
        Instant createdAt
) {

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
