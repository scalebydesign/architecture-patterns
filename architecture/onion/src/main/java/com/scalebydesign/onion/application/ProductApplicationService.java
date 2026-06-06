package com.scalebydesign.onion.application;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.scalebydesign.onion.core.domain.Product;
import com.scalebydesign.onion.core.domain.ProductRepository;
import com.scalebydesign.onion.core.domain.exception.ProductNotFoundException;
import com.scalebydesign.onion.service.ProductService;

@Service
public class ProductApplicationService {

    private static final Logger log = LoggerFactory.getLogger(ProductApplicationService.class);

    private final ProductRepository productRepository;
    private final ProductService productService;

    public ProductApplicationService(ProductRepository productRepository, ProductService productService) {
        this.productRepository = productRepository;
        this.productService = productService;
    }

    public Product createProduct(String name, String description, BigDecimal price, int stock, String category) {
        log.info("Creating product: name={}, category={}, price={}", name, category, price);
        Product product = new Product(name, description, price, stock, category);
        Product saved = productRepository.save(product);
        log.info("Product created successfully: id={}", saved.getId());
        return saved;
    }

    public Product getProduct(UUID id) {
        log.debug("Fetching product: id={}", id);
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id.toString()));
    }

    public List<Product> getProductsByCategory(String category) {
        log.debug("Fetching products by category: {}", category);
        return productRepository.findByCategory(category);
    }

    public List<Product> getAllProducts() {
        log.debug("Fetching all products");
        return productRepository.findAll();
    }

    public Product purchaseProduct(UUID productId, int quantity) {
        log.info("Purchasing product: id={}, quantity={}", productId, quantity);
        Product product = getProduct(productId);
        product.reduceStock(quantity);
        Product saved = productRepository.save(product);
        log.info("Purchase successful: id={}, remainingStock={}", productId, saved.getStockQuantity());
        return saved;
    }

    public Product restockProduct(UUID productId, int quantity, BigDecimal discountPercentage) {
        log.info("Restocking product: id={}, quantity={}, discount={}%", productId, quantity, discountPercentage);
        Product product = getProduct(productId);
        return productService.restockWithDiscount(product, quantity, discountPercentage);
    }

    public List<Product> getLowStockProducts(int threshold) {
        log.debug("Finding low stock products with threshold: {}", threshold);
        return productService.findLowStockProducts(threshold);
    }

    public void deleteProduct(UUID id) {
        log.info("Deleting product: id={}", id);
        productRepository.deleteById(id);
    }
}
