package com.scalebydesign.clean.interface_adapter.presenter;

import com.scalebydesign.clean.entity.User;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * PRESENTER — transforms entity data into response format.
 * Strips sensitive info (password), formats data for the client.
 */
public record UserResponse(
        UUID id,
        String username,
        String email,
        String role,
        boolean active,
        LocalDateTime createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail().getValue(),
                user.getRole().name(),
                user.isActive(),
                user.getCreatedAt()
        );
    }
}
