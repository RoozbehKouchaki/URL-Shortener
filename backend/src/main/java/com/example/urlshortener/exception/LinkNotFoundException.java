package com.example.urlshortener.exception;

/** Short Code unknown, or inactive and treated as absent. Maps to 404. */
public class LinkNotFoundException extends RuntimeException {

    public LinkNotFoundException(String message) {
        super(message);
    }
}
