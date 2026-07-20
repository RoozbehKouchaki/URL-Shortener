package com.example.urlshortener.error;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/**
 * The single, consistent error response structure returned everywhere the
 * service reports a failure.
 *
 * <p>{@code field} is included only when the error concerns a specific input
 * field (for example a validation error) and is omitted otherwise. Stack
 * traces, internal class names, and database details are never included.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(Instant timestamp, int status, String message, String field) {

    /**
     * Build an error response that is not tied to a specific field.
     */
    public static ErrorResponse of(int status, String message) {
        return new ErrorResponse(Instant.now(), status, message, null);
    }

    /**
     * Build an error response for a failure concerning a specific input field.
     */
    public static ErrorResponse of(int status, String message, String field) {
        return new ErrorResponse(Instant.now(), status, message, field);
    }
}
