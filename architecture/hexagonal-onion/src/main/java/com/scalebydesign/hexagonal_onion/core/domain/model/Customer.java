package com.scalebydesign.hexagonal_onion.core.domain.model;

/**
 * CORE DOMAIN - Value Object representing a customer reference.
 * 
 * Innermost layer of the Onion. No external dependencies.
 */
public class Customer {

    private final String customerId;
    private final String email;
    private final CustomerTier tier;

    public Customer(String customerId, String email, CustomerTier tier) {
        this.customerId = customerId;
        this.email = email;
        this.tier = tier;
    }

    public boolean isEligibleForFreeShipping() {
        return tier == CustomerTier.PREMIUM || tier == CustomerTier.VIP;
    }

    // Getters
    public String getCustomerId() { return customerId; }
    public String getEmail() { return email; }
    public CustomerTier getTier() { return tier; }
}
