package com.scalebydesign.hexagonal_onion.application.service;

import java.math.BigDecimal;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.scalebydesign.hexagonal_onion.application.port.inbound.CartUseCase;
import com.scalebydesign.hexagonal_onion.application.port.outbound.CartRepository;
import com.scalebydesign.hexagonal_onion.application.port.outbound.NotificationService;
import com.scalebydesign.hexagonal_onion.core.domain.exception.CartNotFoundException;
import com.scalebydesign.hexagonal_onion.core.domain.exception.EmptyCartException;
import com.scalebydesign.hexagonal_onion.core.domain.model.Customer;
import com.scalebydesign.hexagonal_onion.core.domain.model.CustomerTier;
import com.scalebydesign.hexagonal_onion.core.domain.model.ShoppingCart;
import com.scalebydesign.hexagonal_onion.core.domain.service.CartDomainService;

public class CartApplicationService implements CartUseCase {

    private static final Logger log = LoggerFactory.getLogger(CartApplicationService.class);

    private final CartRepository cartRepository;
    private final NotificationService notificationService;
    private final CartDomainService cartDomainService;

    public CartApplicationService(CartRepository cartRepository,
                                  NotificationService notificationService,
                                  CartDomainService cartDomainService) {
        this.cartRepository = cartRepository;
        this.notificationService = notificationService;
        this.cartDomainService = cartDomainService;
    }

    @Override
    public ShoppingCart createCart(String customerId, String email, String tier) {
        log.info("Creating cart for customer: id={}, tier={}", customerId, tier);
        CustomerTier customerTier = CustomerTier.valueOf(tier.toUpperCase());
        Customer customer = new Customer(customerId, email, customerTier);
        ShoppingCart cart = new ShoppingCart(customer);
        ShoppingCart saved = cartRepository.save(cart);
        log.info("Cart created: cartId={}", saved.getId());
        return saved;
    }

    @Override
    public ShoppingCart addItemToCart(UUID cartId, String productId, String productName, int quantity, BigDecimal price) {
        log.info("Adding item to cart: cartId={}, productId={}, quantity={}", cartId, productId, quantity);
        ShoppingCart cart = findCartOrThrow(cartId);
        cart.addItem(productId, productName, quantity, price);
        return cartRepository.save(cart);
    }

    @Override
    public ShoppingCart removeItemFromCart(UUID cartId, String productId) {
        log.info("Removing item from cart: cartId={}, productId={}", cartId, productId);
        ShoppingCart cart = findCartOrThrow(cartId);
        cart.removeItem(productId);
        return cartRepository.save(cart);
    }

    @Override
    public ShoppingCart updateItemQuantity(UUID cartId, String productId, int quantity) {
        log.info("Updating item quantity: cartId={}, productId={}, newQuantity={}", cartId, productId, quantity);
        ShoppingCart cart = findCartOrThrow(cartId);
        cart.updateItemQuantity(productId, quantity);
        return cartRepository.save(cart);
    }

    @Override
    public ShoppingCart getCart(UUID cartId) {
        log.debug("Fetching cart: cartId={}", cartId);
        return findCartOrThrow(cartId);
    }

    @Override
    public BigDecimal checkout(UUID cartId) {
        log.info("Processing checkout: cartId={}", cartId);
        ShoppingCart cart = findCartOrThrow(cartId);

        if (cart.isEmpty()) {
            log.warn("Checkout attempted on empty cart: cartId={}", cartId);
            throw new EmptyCartException();
        }

        BigDecimal finalTotal = cartDomainService.calculateDiscountedTotal(cart);
        log.info("Checkout total calculated: cartId={}, total={}, items={}", cartId, finalTotal, cart.getItemCount());

        notificationService.sendOrderConfirmation(
                cart.getCustomer().getEmail(),
                String.format("Your order of %s has been placed! Total: $%s",
                        cart.getItemCount() + " items", finalTotal)
        );

        cart.clear();
        cartRepository.save(cart);
        log.info("Checkout complete: cartId={}", cartId);

        return finalTotal;
    }

    @Override
    public void clearCart(UUID cartId) {
        log.info("Clearing cart: cartId={}", cartId);
        ShoppingCart cart = findCartOrThrow(cartId);
        cart.clear();
        cartRepository.save(cart);
    }

    private ShoppingCart findCartOrThrow(UUID cartId) {
        return cartRepository.findById(cartId)
                .orElseThrow(() -> new CartNotFoundException(cartId.toString()));
    }
}
