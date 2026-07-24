package com.example.urlshortener.exception;

/** Well-formed but disallowed host (missing, loopback, or self). Maps to 400. */
public class InvalidUrlException extends RuntimeException {

    public InvalidUrlException(String message) {
        super(message);
    }
}
