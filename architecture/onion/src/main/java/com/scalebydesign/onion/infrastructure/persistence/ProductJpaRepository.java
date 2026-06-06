package com.scalebydesign.onion.infrastructure.persistence;

import com.scalebydesign.onion.core.domain.Product;
import com.scalebydesign.onion.core.domain.ProductRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * OUTERMOST LAYER - Infrastructure Implementation
 * 
 * This implements the ProductRepository interface defined in the CORE domain.
 * 
 * Onion Architecture Key Insight:
 * - The INTERFACE lives in the innermost layer (domain)
 * - The IMPLEMENTATION lives in the outermost layer (infrastructure)
 * - This is exactly Dependency Inversion Principle
 * - Inner layers never know about outer layers
 */
@Repository
public class ProductJpaRepository implements ProductRepository {

    private final SpringDataProductRepository springDataRepo;

    public ProductJpaRepository(SpringDataProductRepository springDataRepo) {
        this.springDataRepo = springDataRepo;
    }

    @Override
    public Product save(Product product) {
        ProductJpaEntity entity = toEntity(product);
        ProductJpaEntity saved = springDataRepo.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Product> findById(UUID id) {
        return springDataRepo.findById(id).map(this::toDomain);
    }

    @Override
    public List<Product> findByCategory(String category) {
        return springDataRepo.findByCategory(category).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Product> findAll() {
        return springDataRepo.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void deleteById(UUID id) {
        springDataRepo.deleteById(id);
    }

    // --- Mapping ---

    private ProductJpaEntity toEntity(Product product) {
        return new ProductJpaEntity(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getCategory()
        );
    }

    private Product toDomain(ProductJpaEntity entity) {
        Product product = new Product(
                entity.getName(),
                entity.getDescription(),
                entity.getPrice(),
                entity.getStockQuantity(),
                entity.getCategory()
        );
        product.setId(entity.getId());
        return product;
    }
}
