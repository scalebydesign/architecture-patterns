package com.scalebydesign.eventsourcing.service;

import com.scalebydesign.eventsourcing.aggregate.Order;
import com.scalebydesign.eventsourcing.event.OrderEvent;
import com.scalebydesign.eventsourcing.store.EventStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * SERVICE — orchestrates commands, loads/saves aggregates via event store.
 * 
 * Pattern:
 * 1. Load events from store
 * 2. Rebuild aggregate by replaying events
 * 3. Execute command (produces new events)
 * 4. Append new events to store
 */
@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final EventStore eventStore;

    public OrderService(EventStore eventStore) {
        this.eventStore = eventStore;
    }

    public UUID createOrder(String customerId) {
        log.info("Creating order for customer: {}", customerId);
        Order order = Order.create(customerId);
        save(order);
        return order.getId();
    }

    public void addItem(UUID orderId, String productId, String name, int quantity, BigDecimal unitPrice) {
        log.info("Adding item: orderId={}, productId={}", orderId, productId);
        Order order = load(orderId);
        order.addItem(productId, name, quantity, unitPrice);
        save(order);
    }

    public void submitOrder(UUID orderId) {
        log.info("Submitting order: {}", orderId);
        Order order = load(orderId);
        order.submit();
        save(order);
    }

    public void approveOrder(UUID orderId) {
        log.info("Approving order: {}", orderId);
        Order order = load(orderId);
        order.approve();
        save(order);
    }

    public void completeOrder(UUID orderId) {
        log.info("Completing order: {}", orderId);
        Order order = load(orderId);
        order.complete();
        save(order);
    }

    public Order getOrder(UUID orderId) {
        return load(orderId);
    }

    public List<OrderEvent> getOrderHistory(UUID orderId) {
        return eventStore.getEvents(orderId);
    }

    private Order load(UUID orderId) {
        List<OrderEvent> events = eventStore.getEvents(orderId);
        if (events.isEmpty()) throw new RuntimeException("Order not found: " + orderId);

        Order order = new Order();
        events.forEach(order::replay);  // Rebuild state from events
        return order;
    }

    private void save(Order order) {
        List<OrderEvent> newEvents = order.getUncommittedEvents();
        if (!newEvents.isEmpty()) {
            eventStore.append(order.getId(), newEvents);
            order.clearUncommittedEvents();
        }
    }
}
