package com.example.urlshortener.error;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/**
 * The single, consistent error body returned everywhere the service reports a
 * failure. {@code field} is populated only for errors tied to a specific input
 * field and is omitted otherwise.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(Instant timestamp, int status, String message, String field) {

    public static ErrorResponse of(int status, String message) {
        return new ErrorResponse(Instant.now(), status, message, null);
    }

    public static ErrorResponse of(int status, String message, String field) {
        return new ErrorResponse(Instant.now(), status, message, field);
    }
}
