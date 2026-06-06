package com.scalebydesign.onion.core.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * INNERMOST LAYER - Repository Interface
 * 
 * In Onion Architecture, repository INTERFACES live in the domain layer.
 * The IMPLEMENTATION lives in the outermost infrastructure layer.
 * 
 * This is the key difference from traditional layered architecture where
 * repositories often live in the data access layer.
 * 
 * The dependency rule: inner layers define contracts, outer layers implement them.
 */
public interface ProductRepository {

    Product save(Product product);

    Optional<Product> findById(UUID id);

    List<Product> findByCategory(String category);

    List<Product> findAll();

    void deleteById(UUID id);
}
