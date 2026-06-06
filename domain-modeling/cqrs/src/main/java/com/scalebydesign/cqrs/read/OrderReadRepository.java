package com.scalebydesign.cqrs.read;

import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class OrderReadRepository {

    private final Map<UUID, OrderReadModel> store = new ConcurrentHashMap<>();

    public void save(OrderReadModel model) {
        store.put(model.id(), model);
    }

    public Optional<OrderReadModel> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    public List<OrderReadModel> findAll() {
        return List.copyOf(store.values());
    }

    public List<OrderReadModel> findByCustomerId(String customerId) {
        return store.values().stream()
                .filter(o -> o.customerId().equals(customerId))
                .toList();
    }

    public List<OrderReadModel> findByStatus(String status) {
        return store.values().stream()
                .filter(o -> o.status().equals(status))
                .toList();
    }
}
