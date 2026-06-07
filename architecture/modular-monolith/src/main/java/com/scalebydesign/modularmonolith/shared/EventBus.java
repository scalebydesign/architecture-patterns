package com.scalebydesign.modularmonolith.shared;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * SHARED KERNEL — In-process event bus for module-to-module communication.
 *
 * In production you might use Spring ApplicationEvents, Kafka, or a message broker.
 * This simple implementation demonstrates the principle: modules are decoupled
 * through events, not direct method calls.
 */
@Component
public class EventBus {

    private static final Logger log = LoggerFactory.getLogger(EventBus.class);

    private final Map<Class<? extends DomainEvent>, List<Consumer<DomainEvent>>> subscribers = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public <T extends DomainEvent> void subscribe(Class<T> eventType, Consumer<T> handler) {
        subscribers.computeIfAbsent(eventType, k -> new ArrayList<>())
                .add((Consumer<DomainEvent>) handler);
        log.debug("Subscribed to {}", eventType.getSimpleName());
    }

    public void publish(DomainEvent event) {
        log.info("Publishing event: {}", event.getClass().getSimpleName());
        List<Consumer<DomainEvent>> handlers = subscribers.getOrDefault(event.getClass(), List.of());
        handlers.forEach(handler -> handler.accept(event));
    }
}
