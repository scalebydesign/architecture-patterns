package com.scalebydesign.eventsourcing.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * EVENT SOURCING - Domain Events
 * 
 * Instead of storing CURRENT STATE, we store the EVENTS that happened.
 * The current state is rebuilt by replaying all events in order.
 * 
 * Events are:
 * - Immutable (past tense — already happened)
 * - Append-only (never deleted or modified)
 * - The source of truth
 */
public sealed interface OrderEvent {

    UUID orderId();
    LocalDateTime occurredAt();

    record OrderCreated(UUID orderId, String customerId, LocalDateTime occurredAt) implements OrderEvent {}

    record ItemAdded(UUID orderId, String productId, String name, int quantity, BigDecimal unitPrice, LocalDateTime occurredAt) implements OrderEvent {}

    record ItemRemoved(UUID orderId, String productId, LocalDateTime occurredAt) implements OrderEvent {}

    record OrderSubmitted(UUID orderId, LocalDateTime occurredAt) implements OrderEvent {}

    record OrderApproved(UUID orderId, LocalDateTime occurredAt) implements OrderEvent {}

    record OrderCompleted(UUID orderId, LocalDateTime occurredAt) implements OrderEvent {}

    record OrderCancelled(UUID orderId, String reason, LocalDateTime occurredAt) implements OrderEvent {}
}
