package com.scalebydesign.layered.controller;

import com.scalebydesign.layered.model.InventoryItem;
import com.scalebydesign.layered.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * TRADITIONAL LAYERED - Controller (Presentation Layer)
 * 
 * In layered architecture: Controller → Service → Repository
 * Each layer depends on the one below. No dependency inversion.
 * 
 * Notice: The controller directly returns the JPA entity as the response.
 * There's no DTO/presenter separation. Simple, but couples API to DB schema.
 */
@RestController
@RequestMapping("/api/layered/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping
    public ResponseEntity<InventoryItem> createItem(@RequestBody CreateItemRequest request) {
        InventoryItem item = inventoryService.createItem(
                request.sku(), request.name(), request.description(),
                request.quantity(), request.unitPrice(), request.warehouse());
        return ResponseEntity.ok(item);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryItem> getItem(@PathVariable UUID id) {
        return ResponseEntity.ok(inventoryService.getItem(id));
    }

    @GetMapping
    public ResponseEntity<List<InventoryItem>> getAllItems() {
        return ResponseEntity.ok(inventoryService.getAllItems());
    }

    @GetMapping("/warehouse/{warehouse}")
    public ResponseEntity<List<InventoryItem>> getByWarehouse(@PathVariable String warehouse) {
        return ResponseEntity.ok(inventoryService.getItemsByWarehouse(warehouse));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<InventoryItem>> getLowStock(@RequestParam(defaultValue = "10") int threshold) {
        return ResponseEntity.ok(inventoryService.getLowStockItems(threshold));
    }

    @PostMapping("/{id}/restock")
    public ResponseEntity<InventoryItem> restock(@PathVariable UUID id, @RequestBody QuantityRequest request) {
        return ResponseEntity.ok(inventoryService.restock(id, request.quantity()));
    }

    @PostMapping("/{id}/withdraw")
    public ResponseEntity<InventoryItem> withdraw(@PathVariable UUID id, @RequestBody QuantityRequest request) {
        return ResponseEntity.ok(inventoryService.withdraw(id, request.quantity()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable UUID id) {
        inventoryService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }

    record CreateItemRequest(String sku, String name, String description,
                             int quantity, BigDecimal unitPrice, String warehouse) {}
    record QuantityRequest(int quantity) {}
}
