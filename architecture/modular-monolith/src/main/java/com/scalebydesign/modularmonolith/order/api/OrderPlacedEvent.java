package com.scalebydesign.modularmonolith.order.api;

import java.time.LocalDateTime;
import java.util.UUID;

import com.scalebydesign.modularmonolith.shared.DomainEvent;

/**
 * ORDER MODULE — Published Event
 *
 * Other modules can subscribe to this event.
 * It's part of the module's public API, alongside OrderFacade and OrderDto.
 */
public record OrderPlacedEvent(
        UUID eventId,
        UUID orderId,
        String customerId,
        String productId,
        int quantity,
        LocalDateTime occurredAt
) implements DomainEvent {}
