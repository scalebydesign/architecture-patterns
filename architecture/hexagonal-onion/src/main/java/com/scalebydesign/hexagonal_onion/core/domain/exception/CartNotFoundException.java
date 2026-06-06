package com.scalebydesign.hexagonal_onion.core.domain.exception;

public class CartNotFoundException extends RuntimeException {

    private final String cartId;

    public CartNotFoundException(String cartId) {
        super("Cart not found: " + cartId);
        this.cartId = cartId;
    }

    public String getCartId() { return cartId; }
}
