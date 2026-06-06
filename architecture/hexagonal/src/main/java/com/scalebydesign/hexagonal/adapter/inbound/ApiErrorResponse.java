package com.scalebydesign.hexagonal.adapter.inbound;

import java.time.LocalDateTime;

/**
 * Standard API error response.
 * This is an adapter concern — the domain doesn't know about HTTP error formats.
 */
public record ApiErrorResponse(
        int status,
        String error,
        String message,
        LocalDateTime timestamp
) {
    public static ApiErrorResponse of(int status, String error, String message) {
        return new ApiErrorResponse(status, error, message, LocalDateTime.now());
    }
}
