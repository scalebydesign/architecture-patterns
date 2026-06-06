package com.scalebydesign.eventsourcing.store;

import com.scalebydesign.eventsourcing.event.OrderEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * EVENT STORE — Append-only storage for events.
 * 
 * In production, this would be backed by EventStoreDB, Kafka, or a DB table.
 * Events are NEVER modified or deleted. Only appended.
 */
@Repository
public class EventStore {

    private static final Logger log = LoggerFactory.getLogger(EventStore.class);

    private final Map<UUID, List<OrderEvent>> streams = new ConcurrentHashMap<>();

    public void append(UUID aggregateId, List<OrderEvent> events) {
        streams.computeIfAbsent(aggregateId, k -> new ArrayList<>()).addAll(events);
        log.info("Appended {} event(s) to stream: {}", events.size(), aggregateId);
        events.forEach(e -> log.debug("  Event: {}", e.getClass().getSimpleName()));
    }

    public List<OrderEvent> getEvents(UUID aggregateId) {
        return streams.getOrDefault(aggregateId, List.of());
    }

    public List<OrderEvent> getAllEvents() {
        return streams.values().stream()
                .flatMap(List::stream)
                .toList();
    }
}
