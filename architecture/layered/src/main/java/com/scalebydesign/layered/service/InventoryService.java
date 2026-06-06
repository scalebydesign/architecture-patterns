package com.scalebydesign.layered.service;

import com.scalebydesign.layered.model.InventoryItem;
import com.scalebydesign.layered.repository.InventoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * TRADITIONAL LAYERED - Service (Business Logic Layer)
 * 
 * In layered architecture, ALL business logic lives in the service.
 * The entity is just a data carrier (Anemic Domain Model).
 * 
 * Notice:
 * - The service directly depends on the repository (Spring Data interface)
 * - The entity has no behavior — the service mutates it
 * - Framework coupling is everywhere (@Service, Spring Data types)
 * 
 * Compare with Hexagonal: there, the entity has behavior and the service orchestrates.
 */
@Service
public class InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);

    private final InventoryRepository inventoryRepository;

    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public InventoryItem createItem(String sku, String name, String description,
                                    int quantity, BigDecimal unitPrice, String warehouse) {
        log.info("Creating inventory item: sku={}, warehouse={}", sku, warehouse);

        if (inventoryRepository.existsBySku(sku)) {
            throw new IllegalArgumentException("Item with SKU already exists: " + sku);
        }
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        if (unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Unit price must be positive");
        }

        InventoryItem item = new InventoryItem(sku, name, description, quantity, unitPrice, warehouse);
        InventoryItem saved = inventoryRepository.save(item);
        log.info("Inventory item created: id={}", saved.getId());
        return saved;
    }

    public InventoryItem getItem(UUID id) {
        return inventoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inventory item not found: " + id));
    }

    public InventoryItem getItemBySku(String sku) {
        return inventoryRepository.findBySku(sku)
                .orElseThrow(() -> new RuntimeException("Inventory item not found with SKU: " + sku));
    }

    public List<InventoryItem> getItemsByWarehouse(String warehouse) {
        log.debug("Fetching items for warehouse: {}", warehouse);
        return inventoryRepository.findByWarehouse(warehouse);
    }

    public List<InventoryItem> getAllItems() {
        return inventoryRepository.findAll();
    }

    public InventoryItem restock(UUID id, int quantity) {
        log.info("Restocking item: id={}, quantity={}", id, quantity);
        if (quantity <= 0) {
            throw new IllegalArgumentException("Restock quantity must be positive");
        }

        InventoryItem item = getItem(id);
        item.setQuantity(item.getQuantity() + quantity);  // Direct mutation — anemic model
        item.setLastRestocked(LocalDateTime.now());

        InventoryItem saved = inventoryRepository.save(item);
        log.info("Restocked: id={}, newQuantity={}", id, saved.getQuantity());
        return saved;
    }

    public InventoryItem withdraw(UUID id, int quantity) {
        log.info("Withdrawing from item: id={}, quantity={}", id, quantity);
        if (quantity <= 0) {
            throw new IllegalArgumentException("Withdraw quantity must be positive");
        }

        InventoryItem item = getItem(id);
        if (item.getQuantity() < quantity) {
            throw new IllegalStateException(
                    "Insufficient stock. Available: " + item.getQuantity() + ", Requested: " + quantity);
        }

        item.setQuantity(item.getQuantity() - quantity);  // Direct mutation — anemic model
        InventoryItem saved = inventoryRepository.save(item);
        log.info("Withdrawn: id={}, remainingQuantity={}", id, saved.getQuantity());
        return saved;
    }

    public List<InventoryItem> getLowStockItems(int threshold) {
        log.debug("Finding low stock items with threshold: {}", threshold);
        return inventoryRepository.findByQuantityLessThan(threshold);
    }

    public void deleteItem(UUID id) {
        log.info("Deleting inventory item: id={}", id);
        inventoryRepository.deleteById(id);
    }
}
