package com.scalebydesign.hexagonal.adapter.outbound;

import com.scalebydesign.hexagonal.domain.model.Order;
import com.scalebydesign.hexagonal.domain.model.OrderItem;
import com.scalebydesign.hexagonal.domain.model.OrderStatus;
import com.scalebydesign.hexagonal.domain.port.outbound.OrderRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * OUTBOUND ADAPTER (Driven Adapter)
 * 
 * Implements the outbound port defined in the domain.
 * Translates between domain model (Order) and infrastructure model (OrderJpaEntity).
 * 
 * Key Hexagonal Principle:
 * - This adapter implements an interface defined in the DOMAIN (OrderRepository port).
 * - The dependency points INWARD: Adapter → Domain, never Domain → Adapter.
 * - If we switch from JPA to MongoDB, only this adapter changes. Domain stays untouched.
 */
@Repository
public class OrderJpaRepository implements OrderRepository {

    private final SpringDataOrderRepository springDataRepo;

    public OrderJpaRepository(SpringDataOrderRepository springDataRepo) {
        this.springDataRepo = springDataRepo;
    }

    @Override
    public Order save(Order order) {
        OrderJpaEntity entity = toEntity(order);
        OrderJpaEntity saved = springDataRepo.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Order> findById(UUID id) {
        return springDataRepo.findById(id).map(this::toDomain);
    }

    // --- Mapping between domain and JPA entity ---

    private OrderJpaEntity toEntity(Order order) {
        OrderJpaEntity entity = new OrderJpaEntity(
                order.getId(),
                order.getCustomerId(),
                order.getStatus().name(),
                order.getCreatedAt()
        );
        entity.setItems(
                order.getItems().stream()
                        .map(item -> new OrderItemJpaEntity(
                                item.getProductId(),
                                item.getProductName(),
                                item.getQuantity(),
                                item.getPrice()
                        ))
                        .toList()
        );
        return entity;
    }

    private Order toDomain(OrderJpaEntity entity) {
        Order order = new Order(entity.getCustomerId());
        order.setId(entity.getId());
        order.setStatus(OrderStatus.valueOf(entity.getStatus()));
        order.setCreatedAt(entity.getCreatedAt());
        order.setItems(
                entity.getItems().stream()
                        .map(item -> new OrderItem(
                                item.getProductId(),
                                item.getProductName(),
                                item.getQuantity(),
                                item.getPrice()
                        ))
                        .toList()
        );
        return order;
    }
}
