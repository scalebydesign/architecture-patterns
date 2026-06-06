package com.scalebydesign.onion.service;

import java.math.BigDecimal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.scalebydesign.onion.core.domain.Product;
import com.scalebydesign.onion.core.domain.ProductRepository;

/**
 * SECOND LAYER - Domain Service
 * 
 * In Onion Architecture, Domain Services contain business logic that
 * spans multiple entities or doesn't naturally belong in a single entity.
 */
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product restockWithDiscount(Product product, int quantity, BigDecimal discountPercentage) {
        log.info("Restocking product: id={}, quantity={}", product.getId(), quantity);
        product.restock(quantity);

        if (quantity >= 100) {
            BigDecimal discountMultiplier = BigDecimal.ONE.subtract(
                    discountPercentage.divide(BigDecimal.valueOf(100))
            );
            BigDecimal newPrice = product.getPrice().multiply(discountMultiplier);
            log.info("Applying bulk discount: oldPrice={}, newPrice={}", product.getPrice(), newPrice);
            product.updatePrice(newPrice);
        }

        return productRepository.save(product);
    }

    public List<Product> findLowStockProducts(int threshold) {
        log.debug("Finding products with stock below threshold: {}", threshold);
        List<Product> lowStock = productRepository.findAll().stream()
                .filter(p -> p.getStockQuantity() < threshold)
                .toList();
        log.info("Found {} low stock products", lowStock.size());
        return lowStock;
    }
}
