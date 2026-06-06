package com.scalebydesign.clean.entity;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ENTITY LAYER (innermost circle in Clean Architecture)
 * 
 * In Clean Architecture (Robert C. Martin / Uncle Bob), the Entity layer
 * contains "Enterprise Business Rules" — business logic that would exist
 * even if there were no software system.
 * 
 * This layer:
 * - Has ZERO dependencies on anything external
 * - Contains critical business rules and data
 * - Is the least likely to change when something external changes
 */
public class User {

    private UUID id;
    private String username;
    private Email email;
    private Password password;
    private UserRole role;
    private boolean active;
    private LocalDateTime createdAt;

    public User(String username, String email, String rawPassword) {
        this.id = UUID.randomUUID();
        this.username = validateUsername(username);
        this.email = new Email(email);
        this.password = Password.fromRaw(rawPassword);
        this.role = UserRole.CUSTOMER;
        this.active = true;
        this.createdAt = LocalDateTime.now();
    }

    // Business rules
    private String validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (username.length() < 3 || username.length() > 30) {
            throw new IllegalArgumentException("Username must be between 3 and 30 characters");
        }
        if (!username.matches("^[a-zA-Z0-9._-]+$")) {
            throw new IllegalArgumentException("Username can only contain letters, numbers, dots, hyphens, and underscores");
        }
        return username.toLowerCase();
    }

    public void deactivate() {
        if (!active) {
            throw new IllegalStateException("User is already deactivated");
        }
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }

    public void changeEmail(String newEmail) {
        this.email = new Email(newEmail);
    }

    public void changePassword(String newRawPassword) {
        this.password = Password.fromRaw(newRawPassword);
    }

    public void promoteToAdmin() {
        this.role = UserRole.ADMIN;
    }

    public boolean checkPassword(String rawPassword) {
        return password.matches(rawPassword);
    }

    // Getters
    public UUID getId() { return id; }
    public String getUsername() { return username; }
    public Email getEmail() { return email; }
    public UserRole getRole() { return role; }
    public boolean isActive() { return active; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // For persistence
    public void setId(UUID id) { this.id = id; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setRole(UserRole role) { this.role = role; }
    public void setActive(boolean active) { this.active = active; }
    public void setEmail(Email email) { this.email = email; }
    public void setPassword(Password password) { this.password = password; }
    public Password getPassword() { return password; }
}
