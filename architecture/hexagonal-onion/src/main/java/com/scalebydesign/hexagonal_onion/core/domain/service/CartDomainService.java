package com.scalebydesign.hexagonal_onion.core.domain.service;

import com.scalebydesign.hexagonal_onion.core.domain.model.CustomerTier;
import com.scalebydesign.hexagonal_onion.core.domain.model.ShoppingCart;

import java.math.BigDecimal;

/**
 * CORE DOMAIN SERVICE (Onion: still in innermost ring)
 * 
 * Contains domain logic that doesn't fit in a single entity.
 * 
 * Combined Architecture:
 * - From ONION: This is part of the domain core. No outward dependencies.
 * - From HEXAGONAL: This is called by the application service, not by adapters directly.
 * 
 * Example: Discount calculation rules that need awareness of both cart and customer.
 */
public class CartDomainService {

    /**
     * Apply tier-based discount to the cart total.
     * Domain rule: VIP gets 10%, Premium gets 5%, Standard gets 0%.
     */
    public BigDecimal calculateDiscountedTotal(ShoppingCart cart) {
        BigDecimal total = cart.calculateTotal();
        BigDecimal discountRate = getDiscountRate(cart.getCustomer().getTier());
        BigDecimal discount = total.multiply(discountRate);
        return total.subtract(discount);
    }

    private BigDecimal getDiscountRate(CustomerTier tier) {
        return switch (tier) {
            case VIP -> new BigDecimal("0.10");
            case PREMIUM -> new BigDecimal("0.05");
            case STANDARD -> BigDecimal.ZERO;
        };
    }
}
