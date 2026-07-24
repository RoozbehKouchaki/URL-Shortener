package com.example.urlshortener.exception;

/** Caller does not own the Link. Maps to 403. */
public class NotLinkOwnerException extends RuntimeException {

    public NotLinkOwnerException(String message) {
        super(message);
    }
}
