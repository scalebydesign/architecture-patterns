package com.scalebydesign.hexagonal_onion.infrastructure.adapter.inbound;

import com.scalebydesign.hexagonal_onion.application.port.inbound.CartUseCase;
import com.scalebydesign.hexagonal_onion.core.domain.model.ShoppingCart;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DRIVING ADAPTER (Hexagonal) / OUTERMOST LAYER (Onion)
 * 
 * Combined Architecture:
 * - From HEXAGONAL: This is a Driving Adapter. It converts HTTP → use case calls.
 *   It depends on the inbound port (CartUseCase), not on the service directly.
 * - From ONION: This lives in the infrastructure layer (outermost ring).
 *   It can depend on inner layers, but nothing depends on it.
 */
@RestController
@RequestMapping("/api/combined/carts")
public class CartController {

    private final CartUseCase cartUseCase;

    public CartController(CartUseCase cartUseCase) {
        this.cartUseCase = cartUseCase;
    }

    @PostMapping
    public ResponseEntity<ShoppingCart> createCart(@RequestBody CreateCartRequest request) {
        ShoppingCart cart = cartUseCase.createCart(request.customerId(), request.email(), request.tier());
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/{cartId}/items")
    public ResponseEntity<ShoppingCart> addItem(@PathVariable UUID cartId, @RequestBody AddItemRequest request) {
        ShoppingCart cart = cartUseCase.addItemToCart(
                cartId, request.productId(), request.productName(), request.quantity(), request.price());
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/{cartId}/items/{productId}")
    public ResponseEntity<ShoppingCart> removeItem(@PathVariable UUID cartId, @PathVariable String productId) {
        return ResponseEntity.ok(cartUseCase.removeItemFromCart(cartId, productId));
    }

    @PutMapping("/{cartId}/items/{productId}")
    public ResponseEntity<ShoppingCart> updateQuantity(@PathVariable UUID cartId,
                                                       @PathVariable String productId,
                                                       @RequestBody UpdateQuantityRequest request) {
        return ResponseEntity.ok(cartUseCase.updateItemQuantity(cartId, productId, request.quantity()));
    }

    @GetMapping("/{cartId}")
    public ResponseEntity<ShoppingCart> getCart(@PathVariable UUID cartId) {
        return ResponseEntity.ok(cartUseCase.getCart(cartId));
    }

    @PostMapping("/{cartId}/checkout")
    public ResponseEntity<CheckoutResponse> checkout(@PathVariable UUID cartId) {
        BigDecimal total = cartUseCase.checkout(cartId);
        return ResponseEntity.ok(new CheckoutResponse(total, "Order placed successfully!"));
    }

    @DeleteMapping("/{cartId}")
    public ResponseEntity<Void> clearCart(@PathVariable UUID cartId) {
        cartUseCase.clearCart(cartId);
        return ResponseEntity.noContent().build();
    }

    // Request/Response DTOs — adapter concern
    record CreateCartRequest(String customerId, String email, String tier) {}
    record AddItemRequest(String productId, String productName, int quantity, BigDecimal price) {}
    record UpdateQuantityRequest(int quantity) {}
    record CheckoutResponse(BigDecimal total, String message) {}
}
