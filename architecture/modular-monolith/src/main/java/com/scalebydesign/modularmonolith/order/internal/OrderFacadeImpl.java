package com.scalebydesign.modularmonolith.order.internal;

import com.scalebydesign.modularmonolith.order.api.OrderDto;
import com.scalebydesign.modularmonolith.order.api.OrderFacade;
import com.scalebydesign.modularmonolith.order.api.OrderPlacedEvent;
import com.scalebydesign.modularmonolith.shared.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * ORDER MODULE — Facade Implementation (internal)
 *
 * Implements the public OrderFacade interface.
 * This is the bridge between the module's public API and its internals.
 * Publishes events so other modules can react.
 */
@Service
class OrderFacadeImpl implements OrderFacade {

    private static final Logger log = LoggerFactory.getLogger(OrderFacadeImpl.class);

    private final OrderRepository orderRepository;
    private final EventBus eventBus;

    private static final Map<String, BigDecimal> PRODUCT_PRICES = Map.of(
            "PROD-1", new BigDecimal("129.99"),
            "PROD-2", new BigDecimal("49.99"),
            "PROD-3", new BigDecimal("299.99")
    );

    OrderFacadeImpl(OrderRepository orderRepository, EventBus eventBus) {
        this.orderRepository = orderRepository;
        this.eventBus = eventBus;
    }

    @Override
    public OrderDto placeOrder(String customerId, String productId, int quantity) {
        BigDecimal unitPrice = PRODUCT_PRICES.getOrDefault(productId, new BigDecimal("9.99"));

        Order order = new Order(customerId, productId, quantity, unitPrice);
        orderRepository.save(order);

        log.info("Order placed: {} for customer {} ({} x {})", order.getId(), customerId, productId, quantity);

        // Publish event — inventory and notification modules will react
        eventBus.publish(new OrderPlacedEvent(
                UUID.randomUUID(), order.getId(), customerId, productId, quantity, LocalDateTime.now()
        ));

        return toDto(order);
    }

    @Override
    public OrderDto getOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        return toDto(order);
    }

    private OrderDto toDto(Order order) {
        return new OrderDto(
                order.getId(),
                order.getCustomerId(),
                order.getProductId(),
                order.getQuantity(),
                order.getTotalPrice(),
                order.getStatus().name()
        );
    }
}
