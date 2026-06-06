package com.scalebydesign.cqrs.read;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * READ MODEL — Denormalized, flat, optimized for QUERIES.
 * No business logic. Pre-calculated fields. Fast reads.
 * 
 * This is a different shape than the write model.
 * The write model has nested LineItems and behavior.
 * The read model is a flat summary — perfect for listing/display.
 */
public record OrderReadModel(
        UUID id,
        String customerId,
        String status,
        BigDecimal total,
        int itemCount,
        LocalDateTime createdAt
) {}
