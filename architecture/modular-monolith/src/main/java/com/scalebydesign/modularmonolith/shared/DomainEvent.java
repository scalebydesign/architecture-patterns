package com.scalebydesign.modularmonolith.shared;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * SHARED KERNEL — Base interface for domain events.
 *
 * Events are the ONLY way modules communicate asynchronously.
 * This lives in the shared kernel because both publisher and subscriber
 * must agree on the event contract.
 */
public interface DomainEvent {
    UUID eventId();
    LocalDateTime occurredAt();
}
