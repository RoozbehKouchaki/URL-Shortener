package com.example.urlshortener.exception;

/**
 * Thrown when a Long URL is well-formed in shape but not allowed (missing host,
 * a loopback host, or the service's own address). Maps to 400.
 */
public class InvalidUrlException extends RuntimeException {

    public InvalidUrlException(String message) {
        super(message);
    }
}
