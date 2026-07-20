package com.example.urlshortener.exception;

/**
 * Thrown when a Short Code does not resolve to a Link the caller may act on
 * (unknown code, or an inactive link treated as absent). Maps to 404.
 */
public class LinkNotFoundException extends RuntimeException {

    public LinkNotFoundException(String message) {
        super(message);
    }
}
