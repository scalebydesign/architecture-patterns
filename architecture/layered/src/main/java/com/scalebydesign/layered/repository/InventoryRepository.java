package com.scalebydesign.layered.repository;

import com.scalebydesign.layered.model.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * TRADITIONAL LAYERED - Repository (Data Access Layer)
 * 
 * In layered architecture, the repository directly extends Spring Data JPA.
 * There's no separate interface in the domain — this IS the interface.
 * 
 * The service layer depends directly on this. No abstraction in between.
 */
@Repository
public interface InventoryRepository extends JpaRepository<InventoryItem, UUID> {

    Optional<InventoryItem> findBySku(String sku);

    List<InventoryItem> findByWarehouse(String warehouse);

    List<InventoryItem> findByQuantityLessThan(int threshold);

    boolean existsBySku(String sku);
}
