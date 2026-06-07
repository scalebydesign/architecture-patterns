package com.scalebydesign.modularmonolith.order.internal;

import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ORDER MODULE — Internal Repository
 *
 * In-memory store. In production, this would be a JPA/DB repository.
 * Package-private — invisible to other modules.
 */
@Repository
class OrderRepository {

    private final Map<UUID, Order> orders = new ConcurrentHashMap<>();

    Order save(Order order) {
        orders.put(order.getId(), order);
        return order;
    }

    Optional<Order> findById(UUID id) {
        return Optional.ofNullable(orders.get(id));
    }
}
