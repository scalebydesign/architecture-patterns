# Onion Architecture

## What is it?

Onion Architecture, introduced by Jeffrey Palermo in 2008, organizes code into **concentric layers** like an onion. The core domain sits at the center, and each outer layer can only depend on layers closer to the center — never the other way around.

## Core Idea

Think of concentric circles. The innermost circle is your domain — your business rules. Each ring outward adds more "infrastructure" and "framework" concerns. **Dependencies only point inward.**

```
┌─────────────────────────────────────────────────────────────┐
│                    INFRASTRUCTURE                            │
│  (Controllers, JPA Repositories, External APIs, Config)     │
│                                                             │
│   ┌─────────────────────────────────────────────────────┐   │
│   │              APPLICATION SERVICES                    │   │
│   │  (Use case orchestration, transaction management)   │   │
│   │                                                     │   │
│   │   ┌─────────────────────────────────────────────┐   │   │
│   │   │           DOMAIN SERVICES                   │   │   │
│   │   │  (Business logic spanning multiple entities)│   │   │
│   │   │                                             │   │   │
│   │   │   ┌─────────────────────────────────────┐   │   │   │
│   │   │   │         DOMAIN MODEL                │   │   │   │
│   │   │   │  (Entities, Value Objects, Enums)   │   │   │   │
│   │   │   │         *** CENTER ***              │   │   │   │
│   │   │   └─────────────────────────────────────┘   │   │   │
│   │   │                                             │   │   │
│   │   └─────────────────────────────────────────────┘   │   │
│   │                                                     │   │
│   └─────────────────────────────────────────────────────┘   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```


## The Four Layers

### Layer 1: Domain Model (Innermost)
- Entities, Value Objects, Enums
- Contains business rules and invariants
- **ZERO** dependencies on anything else
- Example: `Product.java`

### Layer 2: Domain Services
- Business logic that doesn't fit in a single entity
- Can depend on Domain Model only
- Pure business operations — no framework annotations
- Example: `ProductService.java`

### Layer 3: Application Services
- Orchestrates use cases
- Coordinates Domain Services and Domain Model
- Handles cross-cutting concerns (transactions, security)
- Example: `ProductApplicationService.java`

### Layer 4: Infrastructure (Outermost)
- Frameworks (Spring MVC, JPA)
- Database access implementations
- External service integrations
- Controllers, configuration
- Examples: `ProductController.java`, `ProductJpaRepository.java`

## The Critical Rule

```
Inner layers NEVER depend on outer layers.
```

| Layer | Can depend on | Cannot depend on |
|-------|---------------|------------------|
| Domain Model | Nothing | Everything else |
| Domain Services | Domain Model | Application, Infrastructure |
| Application Services | Domain Model, Domain Services | Infrastructure |
| Infrastructure | Everything inner | — |

## Where do INTERFACES live?

This is the key insight that differentiates Onion from traditional layered architecture:

> **Repository INTERFACES live in the DOMAIN (inner layer).**  
> **Repository IMPLEMENTATIONS live in INFRASTRUCTURE (outer layer).**

In traditional layered architecture, the repository interface would be in the "data access layer." In Onion, it's in the domain — because the domain defines what it needs.


## Project Structure Mapping

```
onion/
├── core/
│   └── domain/                              ← LAYER 1: Domain Model (innermost)
│       ├── Product.java                     ← Entity with business rules
│       └── ProductRepository.java           ← Interface (contract) defined by domain
│
├── service/                                 ← LAYER 2: Domain Services
│   └── ProductService.java                  ← Business logic (no Spring annotations!)
│
├── application/                             ← LAYER 3: Application Services
│   └── ProductApplicationService.java       ← Use case orchestration
│
└── infrastructure/                          ← LAYER 4: Infrastructure (outermost)
    ├── persistence/
    │   ├── ProductJpaEntity.java            ← JPA entity (infrastructure concern)
    │   ├── ProductJpaRepository.java        ← Implements domain interface
    │   └── SpringDataProductRepository.java ← Spring Data internal detail
    ├── web/
    │   └── ProductController.java           ← REST controller
    └── config/
        └── BeanConfig.java                  ← Spring wiring (composition root)
```

## How it works in this example

### Flow: Create a Product
```
HTTP POST /api/onion/products
    → ProductController (Layer 4: Infrastructure)
        → ProductApplicationService.createProduct() (Layer 3: Application)
            → new Product(...) (Layer 1: Domain — validates business rules)
            → ProductRepository.save() (Layer 1: Interface)
                → ProductJpaRepository (Layer 4: Implementation)
                    → Database
```

### Flow: Restock with Discount
```
ProductApplicationService.restockProduct() (Layer 3)
    → ProductService.restockWithDiscount() (Layer 2: Domain Service)
        → product.restock() (Layer 1: Entity logic)
        → product.updatePrice() (Layer 1: Entity logic)
        → ProductRepository.save() (Layer 1: Interface → Layer 4: Implementation)
```

### Notice the dependency direction:
- Controller → ApplicationService → DomainService → DomainModel
- All arrows point INWARD
- Infrastructure implements interfaces defined in inner layers


## Onion vs Traditional Layered Architecture

| Aspect | Traditional Layered | Onion |
|--------|-------------------|-------|
| Repository interface | In data access layer | In domain layer |
| Domain depends on | Data layer | Nothing |
| Framework coupling | Everywhere | Only outermost layer |
| Dependency direction | Top → Bottom (UI → BL → DA) | Outside → Inside |
| Testability | Need DB for service tests | Mock inner interfaces easily |

## Key Observation: ProductService has NO @Service annotation

```java
// ProductService.java — notice: NO Spring annotations
public class ProductService {
    private final ProductRepository productRepository;
    
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
}
```

It's wired in `BeanConfig.java` (infrastructure layer):
```java
@Bean
public ProductService productService(ProductRepository productRepository) {
    return new ProductService(productRepository);
}
```

This proves the domain service has NO dependency on Spring. You could use this class in a completely different framework.

## Why use Onion Architecture?

### ✅ Benefits
1. **Domain-centric** — Business logic is the center of your universe.
2. **Testable** — Inner layers can be tested without infrastructure.
3. **Flexible** — Change database, framework, or UI without touching domain.
4. **Clear dependency rules** — Easy to enforce and understand.
5. **Long-lived** — Domain code outlives frameworks and tools.

### ❌ When NOT to use
1. Very simple CRUD apps with no real domain logic.
2. When the team isn't familiar with DIP (Dependency Inversion).
3. Rapid throwaway prototypes.

## Running this module

```bash
./gradlew :onion:bootRun
```

Try the API:
```bash
# Create product
curl -X POST http://localhost:8082/api/onion/products \
  -H "Content-Type: application/json" \
  -d '{"name": "Wireless Mouse", "description": "Ergonomic mouse", "price": 29.99, "stockQuantity": 100, "category": "electronics"}'

# Purchase
curl -X POST http://localhost:8082/api/onion/products/{id}/purchase \
  -H "Content-Type: application/json" \
  -d '{"quantity": 5}'

# Check low stock
curl http://localhost:8082/api/onion/products/low-stock?threshold=10
```
