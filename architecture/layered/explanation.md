# Traditional Layered Architecture

## What is it?

The most common architecture pattern in enterprise software. Code is organized into horizontal layers, each with a specific responsibility. Each layer depends on the layer directly below it.

## The Layers

```
┌─────────────────────────────────┐
│     PRESENTATION LAYER          │  ← Controllers, REST endpoints
│     (Controller)                │
└──────────────┬──────────────────┘
               │ depends on
┌──────────────▼──────────────────┐
│     BUSINESS LOGIC LAYER        │  ← Services, validation, rules
│     (Service)                   │
└──────────────┬──────────────────┘
               │ depends on
┌──────────────▼──────────────────┐
│     DATA ACCESS LAYER           │  ← Repositories, DB queries
│     (Repository)                │
└──────────────┬──────────────────┘
               │ depends on
┌──────────────▼──────────────────┐
│     DATABASE                    │  ← H2, PostgreSQL, MySQL
└─────────────────────────────────┘
```

## Key Characteristics

1. **Top-down dependencies** — each layer depends on the one below
2. **No dependency inversion** — the repository defines its own interface
3. **Anemic Domain Model** — entities are data bags, services have all logic
4. **Framework-coupled** — JPA annotations in the model, Spring everywhere
5. **Simple and familiar** — most developers know this pattern


## Project Structure

```
layered/
├── controller/                      # Presentation Layer
│   ├── InventoryController.java    # REST endpoints
│   └── GlobalExceptionHandler.java # Error mapping
│
├── service/                         # Business Logic Layer
│   └── InventoryService.java       # ALL business logic here
│
├── repository/                      # Data Access Layer
│   └── InventoryRepository.java    # Spring Data JPA interface
│
└── model/                           # Shared entity (used by all layers)
    └── InventoryItem.java          # JPA entity — shared across layers
```

## How it compares to Hexagonal/Onion

| Aspect | Layered | Hexagonal/Onion/Clean |
|--------|---------|----------------------|
| Dependency direction | Top → Bottom | Outside → Inside |
| Entity has behavior | ❌ No (anemic) | ✅ Yes (rich) |
| Entity has JPA annotations | ✅ Yes | ❌ No |
| Repository interface location | Data layer | Domain layer |
| Service depends on | Spring Data repo directly | Abstract port/interface |
| Testability | Need DB or mocks of Spring Data | Mock simple interfaces |
| Swappability | Hard — coupled to framework | Easy — swap adapters |
| Simplicity | ✅ Simple | ❌ More classes, more layers |

## Notice: The Anemic Domain Model

In this example, `InventoryItem` is a pure data bag:

```java
@Entity
public class InventoryItem {
    private int quantity;
    // Just getters and setters. No behavior.
}
```

The service does all the work:

```java
@Service
public class InventoryService {
    public InventoryItem restock(UUID id, int quantity) {
        InventoryItem item = getItem(id);
        item.setQuantity(item.getQuantity() + quantity);  // Direct mutation
        item.setLastRestocked(LocalDateTime.now());
        return inventoryRepository.save(item);
    }
}
```

Compare with Hexagonal's `Order.java` where `order.confirm()` enforces its own rules.

## When to use Layered Architecture

### ✅ Use when:
- Simple CRUD applications
- Small team with junior developers
- Rapid prototyping / MVPs
- Thin business logic (mostly data in/out)
- Short-lived projects

### ❌ Avoid when:
- Complex domain logic with many business rules
- Need to swap databases or frameworks
- Multiple entry points (REST + queue + CLI)
- Long-lived products that will evolve significantly
- Need high testability without infrastructure

## The honest truth

Most applications START as layered and MIGRATE to Hexagonal/Onion/Clean when complexity grows. There's nothing wrong with layered for simple apps — the problem is when it doesn't evolve alongside the domain complexity.

## Running this module

```bash
./gradlew :layered:bootRun
```

```bash
# Create item
curl -X POST http://localhost:8085/api/layered/inventory \
  -H "Content-Type: application/json" \
  -d '{"sku": "WH-001", "name": "Widget", "description": "Blue widget", "quantity": 100, "unitPrice": 9.99, "warehouse": "NYC"}'

# Restock
curl -X POST http://localhost:8085/api/layered/inventory/{id}/restock \
  -H "Content-Type: application/json" -d '{"quantity": 50}'

# Withdraw (try more than available → 409)
curl -X POST http://localhost:8085/api/layered/inventory/{id}/withdraw \
  -H "Content-Type: application/json" -d '{"quantity": 9999}'

# Low stock items
curl http://localhost:8085/api/layered/inventory/low-stock?threshold=10
```
