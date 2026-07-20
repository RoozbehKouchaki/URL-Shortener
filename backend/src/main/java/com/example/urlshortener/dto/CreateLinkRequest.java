package com.example.urlshortener.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request body for creating a Link. Basic shape (presence, length, http/https
 * scheme) is enforced here; host-level rules (loopback and self-address) are
 * enforced in the service.
 */
public record CreateLinkRequest(

        @NotBlank(message = "Long URL is required.")
        @Size(max = 2048, message = "Long URL must be at most 2048 characters.")
        @Pattern(regexp = "^https?://.+", message = "Long URL must be a valid http or https address.")
        String longUrl
) {
}
