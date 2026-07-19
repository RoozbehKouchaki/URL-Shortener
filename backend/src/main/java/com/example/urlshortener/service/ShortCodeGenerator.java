package com.example.urlshortener.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * Produces a Short Code as exactly seven characters drawn from the
 * 62-character base62 set (digits, lowercase letters, uppercase letters),
 * chosen with {@link SecureRandom}.
 *
 * <p>Using a secure random generator (rather than encoding a database id)
 * means codes are not sequential and cannot be guessed from one another.
 */
@Component
public class ShortCodeGenerator {

    /** The 62-character base62 alphabet: digits, lowercase, then uppercase. */
    private static final char[] BASE62 =
            "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    /** Exactly seven characters per Short Code. */
    static final int CODE_LENGTH = 7;

    private final SecureRandom random = new SecureRandom();

    /**
     * Generate a random Short Code of exactly seven base62 characters.
     *
     * @return a new seven-character base62 Short Code
     */
    public String generate() {
        char[] code = new char[CODE_LENGTH];
        for (int i = 0; i < CODE_LENGTH; i++) {
            code[i] = BASE62[random.nextInt(BASE62.length)];
        }
        return new String(code);
    }
}
