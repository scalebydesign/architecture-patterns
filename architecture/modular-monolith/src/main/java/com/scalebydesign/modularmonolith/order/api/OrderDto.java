package com.scalebydesign.modularmonolith.order.api;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * ORDER MODULE — Public DTO
 *
 * Other modules see this, never the internal Order entity.
 * This is part of the module's published contract.
 */
public record OrderDto(
        UUID orderId,
        String customerId,
        String productId,
        int quantity,
        BigDecimal totalPrice,
        String status
) {}
