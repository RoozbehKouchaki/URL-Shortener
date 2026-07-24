package com.example.urlshortener.exception;

/** No unique Short Code within the allowed attempts. Maps to 500. */
public class ShortCodeGenerationException extends RuntimeException {

    public ShortCodeGenerationException(String message) {
        super(message);
    }
}
