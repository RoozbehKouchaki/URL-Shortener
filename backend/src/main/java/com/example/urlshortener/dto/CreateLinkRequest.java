package com.example.urlshortener.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Shape only; the self-address host rule is enforced in the service. */
public record CreateLinkRequest(

        @NotBlank(message = "Long URL is required.")
        @Size(max = 2048, message = "Long URL must be at most 2048 characters.")
        @Pattern(regexp = "^https?://.+", message = "Long URL must be a valid http or https address.")
        String longUrl
) {
}
