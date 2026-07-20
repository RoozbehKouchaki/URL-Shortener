package com.example.urlshortener.exception;

/**
 * Thrown when a unique Short Code could not be produced within the allowed
 * number of attempts. Maps to 500.
 */
public class ShortCodeGenerationException extends RuntimeException {

    public ShortCodeGenerationException(String message) {
        super(message);
    }
}
