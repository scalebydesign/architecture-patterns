package com.scalebydesign.modularmonolith.web;

import com.scalebydesign.modularmonolith.inventory.api.InventoryFacade;
import com.scalebydesign.modularmonolith.order.api.OrderDto;
import com.scalebydesign.modularmonolith.order.api.OrderFacade;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST Controller — thin HTTP layer.
 *
 * This demonstrates that the controller only talks to module FACADES.
 * It has no access to Order.java, StockRepository, etc.
 */
@RestController
@RequestMapping("/api/modular")
public class OrderController {

    private final OrderFacade orderFacade;
    private final InventoryFacade inventoryFacade;

    public OrderController(OrderFacade orderFacade, InventoryFacade inventoryFacade) {
        this.orderFacade = orderFacade;
        this.inventoryFacade = inventoryFacade;
    }

    @PostMapping("/orders")
    public ResponseEntity<OrderDto> placeOrder(@RequestBody PlaceOrderRequest request) {
        OrderDto order = orderFacade.placeOrder(request.customerId(), request.productId(), request.quantity());
        return ResponseEntity.ok(order);
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<OrderDto> getOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(orderFacade.getOrder(id));
    }

    @GetMapping("/inventory/{productId}")
    public ResponseEntity<StockResponse> getStock(@PathVariable String productId) {
        int stock = inventoryFacade.getStock(productId);
        return ResponseEntity.ok(new StockResponse(productId, stock));
    }

    record PlaceOrderRequest(String customerId, String productId, int quantity) {}
    record StockResponse(String productId, int available) {}
}
