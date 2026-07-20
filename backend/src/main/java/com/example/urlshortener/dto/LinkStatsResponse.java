package com.example.urlshortener.dto;

/**
 * Click statistics for a single Link.
 */
public record LinkStatsResponse(String shortCode, long clickCount) {
}
