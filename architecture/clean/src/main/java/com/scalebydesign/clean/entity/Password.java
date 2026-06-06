package com.scalebydesign.clean.entity;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * VALUE OBJECT — Encapsulates password hashing logic.
 * 
 * In real production code, use BCrypt/Argon2. 
 * Using SHA-256 here for simplicity (no external dependencies in entity layer).
 */
public class Password {

    private final String hashedValue;

    private Password(String hashedValue) {
        this.hashedValue = hashedValue;
    }

    public static Password fromRaw(String rawPassword) {
        validateStrength(rawPassword);
        return new Password(hash(rawPassword));
    }

    public static Password fromHash(String hashedValue) {
        return new Password(hashedValue);
    }

    public boolean matches(String rawPassword) {
        return hashedValue.equals(hash(rawPassword));
    }

    private static void validateStrength(String rawPassword) {
        if (rawPassword == null || rawPassword.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }
        if (!rawPassword.matches(".*[A-Z].*")) {
            throw new IllegalArgumentException("Password must contain at least one uppercase letter");
        }
        if (!rawPassword.matches(".*[0-9].*")) {
            throw new IllegalArgumentException("Password must contain at least one digit");
        }
    }

    private static String hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    public String getHashedValue() { return hashedValue; }
}
