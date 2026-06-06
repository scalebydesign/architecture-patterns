package com.scalebydesign.hexagonal.adapter.inbound;

import com.scalebydesign.hexagonal.domain.model.Order;
import com.scalebydesign.hexagonal.domain.port.inbound.OrderUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * INBOUND ADAPTER (Driving Adapter)
 * 
 * This is how the outside world DRIVES our application.
 * - It converts HTTP requests into calls to the INBOUND PORT (OrderUseCase).
 * - It knows about the web framework (Spring MVC), but the domain doesn't.
 * 
 * Key Hexagonal Principle:
 * - This adapter depends on the PORT, not on the service directly.
 * - The port is defined in the domain — so the dependency points inward.
 */
@RestController
@RequestMapping("/api/hexagonal/orders")
public class OrderController {

    private final OrderUseCase orderUseCase;

    public OrderController(OrderUseCase orderUseCase) {
        this.orderUseCase = orderUseCase;
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody CreateOrderRequest request) {
        Order order = orderUseCase.createOrder(request.customerId());
        return ResponseEntity.ok(order);
    }

    @PostMapping("/{orderId}/items")
    public ResponseEntity<Order> addItem(@PathVariable UUID orderId, @RequestBody AddItemRequest request) {
        Order order = orderUseCase.addItemToOrder(
                orderId,
                request.productId(),
                request.productName(),
                request.quantity(),
                request.price()
        );
        return ResponseEntity.ok(order);
    }

    @PostMapping("/{orderId}/confirm")
    public ResponseEntity<Order> confirmOrder(@PathVariable UUID orderId) {
        return ResponseEntity.ok(orderUseCase.confirmOrder(orderId));
    }

    @PostMapping("/{orderId}/pay")
    public ResponseEntity<Order> payOrder(@PathVariable UUID orderId) {
        return ResponseEntity.ok(orderUseCase.payOrder(orderId));
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<Order> cancelOrder(@PathVariable UUID orderId) {
        return ResponseEntity.ok(orderUseCase.cancelOrder(orderId));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrder(@PathVariable UUID orderId) {
        return ResponseEntity.ok(orderUseCase.getOrder(orderId));
    }

    // Request DTOs — adapter concern, not domain
    record CreateOrderRequest(String customerId) {}
    record AddItemRequest(String productId, String productName, int quantity, BigDecimal price) {}
}
