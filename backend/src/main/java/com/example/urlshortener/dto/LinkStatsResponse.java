package com.example.urlshortener.dto;

public record LinkStatsResponse(String shortCode, long clickCount) {
}
