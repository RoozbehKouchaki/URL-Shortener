package com.example.urlshortener.exception;

/**
 * Thrown when an authenticated user attempts to act on a Link owned by someone
 * else. Maps to 403.
 */
public class NotLinkOwnerException extends RuntimeException {

    public NotLinkOwnerException(String message) {
        super(message);
    }
}
