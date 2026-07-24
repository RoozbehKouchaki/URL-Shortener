package com.example.urlshortener.dto;

/** {@code expiresIn} is in seconds. */
public record LoginResponse(String accessToken, String tokenType, long expiresIn, String username) {

    private static final String BEARER = "Bearer";

    public static LoginResponse bearer(String accessToken, long expiresIn, String username) {
        return new LoginResponse(accessToken, BEARER, expiresIn, username);
    }
}
