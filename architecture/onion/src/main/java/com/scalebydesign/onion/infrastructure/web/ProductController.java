package com.scalebydesign.onion.infrastructure.web;

import com.scalebydesign.onion.application.ProductApplicationService;
import com.scalebydesign.onion.core.domain.Product;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * OUTERMOST LAYER - Infrastructure (Web)
 * 
 * In Onion Architecture, controllers live in the infrastructure layer.
 * They depend on the Application Service (inner layer) — never the other way around.
 * 
 * Notice: No "port" or "adapter" terminology. Onion focuses on LAYERS, not ports.
 */
@RestController
@RequestMapping("/api/onion/products")
public class ProductController {

    private final ProductApplicationService productApplicationService;

    public ProductController(ProductApplicationService productApplicationService) {
        this.productApplicationService = productApplicationService;
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody CreateProductRequest request) {
        Product product = productApplicationService.createProduct(
                request.name(),
                request.description(),
                request.price(),
                request.stockQuantity(),
                request.category()
        );
        return ResponseEntity.ok(product);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable UUID id) {
        return ResponseEntity.ok(productApplicationService.getProduct(id));
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productApplicationService.getAllProducts());
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<Product>> getByCategory(@PathVariable String category) {
        return ResponseEntity.ok(productApplicationService.getProductsByCategory(category));
    }

    @PostMapping("/{id}/purchase")
    public ResponseEntity<Product> purchaseProduct(@PathVariable UUID id, @RequestBody PurchaseRequest request) {
        return ResponseEntity.ok(productApplicationService.purchaseProduct(id, request.quantity()));
    }

    @PostMapping("/{id}/restock")
    public ResponseEntity<Product> restockProduct(@PathVariable UUID id, @RequestBody RestockRequest request) {
        return ResponseEntity.ok(productApplicationService.restockProduct(id, request.quantity(), request.discountPercentage()));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<Product>> getLowStockProducts(@RequestParam(defaultValue = "10") int threshold) {
        return ResponseEntity.ok(productApplicationService.getLowStockProducts(threshold));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID id) {
        productApplicationService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    // Request DTOs
    record CreateProductRequest(String name, String description, BigDecimal price, int stockQuantity, String category) {}
    record PurchaseRequest(int quantity) {}
    record RestockRequest(int quantity, BigDecimal discountPercentage) {}
}
