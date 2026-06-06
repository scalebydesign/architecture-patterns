package com.scalebydesign.cqrs.write;

import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class OrderWriteRepository {

    private final Map<UUID, Order> store = new ConcurrentHashMap<>();

    public void save(Order order) {
        store.put(order.getId(), order);
    }

    public Optional<Order> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }
}
