package com.example.urlshortener.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignupRequest(

        @NotBlank(message = "Username is required.")
        @Size(min = 3, max = 30, message = "Username must be 3-30 characters.")
        @Pattern(regexp = "[A-Za-z0-9_-]+", message = "Username may contain letters, digits, '-' and '_' only.")
        String username,

        @NotBlank(message = "Password is required.")
        @Size(min = 8, max = 72, message = "Password must be 8-72 characters.")
        String password) {
}
