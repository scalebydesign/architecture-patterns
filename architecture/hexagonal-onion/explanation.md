# Hexagonal + Onion Architecture (Combined)

## Why combine them?

Hexagonal and Onion are not competing patterns — they're **complementary**. In practice, most real-world projects use elements of both:

| From Hexagonal | From Onion |
|----------------|------------|
| Ports & Adapters pattern at boundaries | Concentric layer organization |
| Explicit driving/driven direction | Domain at the center |
| Swappable infrastructure via ports | Clear dependency rules between layers |

## How they fit together

```
┌──────────────────────────────────────────────────────────────────────┐
│  INFRASTRUCTURE (Onion: outermost layer)                             │
│  ┌──────────────────────────────────────────────────────────────┐    │
│  │ Driving Adapters (Hexagonal)  │  Driven Adapters (Hexagonal) │    │
│  │ • CartController              │  • CartInMemoryRepository    │    │
│  │                               │  • EmailNotificationAdapter  │    │
│  └───────────────┬───────────────┴──────────────▲───────────────┘    │
│                  │ calls                         │ implements         │
│  ┌───────────────▼──────────────────────────────┴───────────────┐    │
│  │  APPLICATION LAYER (Onion: middle ring)                       │    │
│  │  ┌─────────────────────────────────────────────────────────┐  │    │
│  │  │ Inbound Ports       │  Outbound Ports                   │  │    │
│  │  │ • CartUseCase       │  • CartRepository                 │  │    │
│  │  │                     │  • NotificationService            │  │    │
│  │  └─────────────────────┴───────────────────────────────────┘  │    │
│  │                                                               │    │
│  │  CartApplicationService (implements CartUseCase,              │    │
│  │                          uses outbound ports)                 │    │
│  └───────────────────────────────┬───────────────────────────────┘    │
│                                  │ uses                               │
│  ┌───────────────────────────────▼───────────────────────────────┐    │
│  │  CORE DOMAIN (Onion: innermost ring)                          │    │
│  │  • ShoppingCart (Aggregate Root)                              │    │
│  │  • Customer, CartItem (Value Objects)                         │    │
│  │  • CartDomainService (Domain Service)                         │    │
│  │                                                               │    │
│  │  *** ZERO external dependencies ***                           │    │
│  └───────────────────────────────────────────────────────────────┘    │
└──────────────────────────────────────────────────────────────────────┘
```


## What each pattern contributes

### From Onion Architecture:
- **Layered structure** — core domain → domain services → application → infrastructure
- **Dependency rule** — all dependencies point inward
- **Domain at the center** — no framework code in the core

### From Hexagonal Architecture:
- **Ports** — explicit interfaces at the application boundary
- **Driving/Driven distinction** — clear direction of communication
- **Adapters** — pluggable implementations in infrastructure layer

### The synthesis:
- Onion's **layers** give us the overall structure
- Hexagonal's **ports** give us explicit contracts at layer boundaries
- Hexagonal's **adapters** sit in Onion's outermost infrastructure layer

## Project Structure Mapping

```
hexagonal-onion/
├── core/                                    ← ONION: Innermost layer
│   └── domain/
│       ├── model/
│       │   ├── ShoppingCart.java            ← Aggregate Root (business rules)
│       │   ├── CartItem.java               ← Value Object
│       │   ├── Customer.java               ← Value Object
│       │   └── CustomerTier.java           ← Enum
│       └── service/
│           └── CartDomainService.java       ← Domain Service (no dependencies)
│
├── application/                             ← ONION: Application layer
│   ├── port/                                ← HEXAGONAL: Port definitions
│   │   ├── inbound/
│   │   │   └── CartUseCase.java            ← Driving Port (what can outside do?)
│   │   └── outbound/
│   │       ├── CartRepository.java         ← Driven Port (what does app need?)
│   │       └── NotificationService.java    ← Driven Port
│   └── service/
│       └── CartApplicationService.java     ← Implements inbound port, uses outbound ports
│
└── infrastructure/                          ← ONION: Outermost layer
    ├── adapter/                             ← HEXAGONAL: Adapter implementations
    │   ├── inbound/
    │   │   └── CartController.java         ← Driving Adapter (HTTP → port)
    │   └── outbound/
    │       ├── CartInMemoryRepository.java  ← Driven Adapter (port → storage)
    │       └── EmailNotificationAdapter.java ← Driven Adapter (port → email)
    └── config/
        └── BeanConfig.java                  ← Composition Root (wires everything)
```


## How it works: Checkout Flow

```
1. HTTP POST /api/combined/carts/{id}/checkout
   └── CartController (Infrastructure: Driving Adapter)
       └── calls CartUseCase.checkout(cartId) (Application: Inbound Port)

2. CartApplicationService.checkout() (Application: Service)
   ├── cartRepository.findById() (Application: Outbound Port)
   │   └── CartInMemoryRepository (Infrastructure: Driven Adapter)
   │
   ├── cartDomainService.calculateDiscountedTotal(cart) (Core: Domain Service)
   │   └── cart.calculateTotal() (Core: Entity logic)
   │   └── applies tier-based discount (Core: Business rule)
   │
   ├── notificationService.sendOrderConfirmation() (Application: Outbound Port)
   │   └── EmailNotificationAdapter (Infrastructure: Driven Adapter)
   │
   └── cart.clear() + cartRepository.save() (Domain logic + persistence)

3. Response flows back up through the layers
```

## Key Design Decisions

### 1. CartApplicationService has NO Spring annotations
```java
public class CartApplicationService implements CartUseCase {
    // Pure Java. No @Service, no @Transactional, no framework coupling.
}
```
It's wired in `BeanConfig.java`. This means:
- You can unit test it with plain JUnit — no Spring context needed
- You can reuse it in a non-Spring application

### 2. CartDomainService is completely isolated
```java
public class CartDomainService {
    // No dependencies on repositories, ports, or framework
    // Pure business logic: discount calculations
}
```

### 3. Ports live in the Application layer, not the Domain Core
Unlike pure Onion (where interfaces are in domain), here ports sit in the application layer. This is intentional — it separates:
- **Domain interfaces** (what the domain needs internally)
- **Application ports** (how the application communicates with the outside world)

### 4. Adapters are in Onion's Infrastructure layer
Hexagonal's adapters naturally map to Onion's outermost layer. Both patterns agree: infrastructure details belong at the boundary.

## Comparing all three approaches

| Aspect | Pure Hexagonal | Pure Onion | Combined |
|--------|---------------|------------|----------|
| Focus | Ports & Adapters | Layers & Rings | Both |
| Interface location | Domain ports | Domain layer | Application ports + Domain core |
| Layer count | Not prescribed | 4 explicit layers | 3 layers + ports |
| Direction language | Driving/Driven | Inner/Outer | Both terms used |
| Domain services | Not explicit | Explicit layer | Explicit in core |
| Best for | Integration-heavy apps | Domain-heavy apps | Complex apps with both |


## When to use the combined approach?

### ✅ Use when:
1. Your application has **rich domain logic** AND **multiple integration points** (APIs, queues, databases).
2. You want the discipline of Onion's layers AND the flexibility of Hexagonal's swappable adapters.
3. Your team already understands both patterns individually.
4. You're building a long-lived product that will outlive its current technology stack.

### ❌ Don't use when:
1. Your team is new to DDD — start with one pattern first.
2. The application is a thin wrapper around a database (CRUD).
3. You're building a proof-of-concept or throwaway prototype.

## The Composition Root Pattern

Notice `BeanConfig.java` — this is the **Composition Root**:

```java
@Configuration
public class BeanConfig {

    @Bean
    public CartDomainService cartDomainService() {
        return new CartDomainService();
    }

    @Bean
    public CartApplicationService cartApplicationService(
            CartRepository cartRepository,
            NotificationService notificationService,
            CartDomainService cartDomainService) {
        return new CartApplicationService(cartRepository, notificationService, cartDomainService);
    }
}
```

All wiring happens in ONE place in the outermost layer. This means:
- Inner layers are framework-agnostic
- Swapping implementations is a config change, not a code change
- Dependencies are explicit and visible

## Running this module

```bash
./gradlew :hexagonal-onion:bootRun
```

Try the API:
```bash
# Create cart
curl -X POST http://localhost:8083/api/combined/carts \
  -H "Content-Type: application/json" \
  -d '{"customerId": "cust-1", "email": "john@example.com", "tier": "VIP"}'

# Add items
curl -X POST http://localhost:8083/api/combined/carts/{cartId}/items \
  -H "Content-Type: application/json" \
  -d '{"productId": "p1", "productName": "Headphones", "quantity": 2, "price": 49.99}'

# Checkout (applies VIP discount + sends notification)
curl -X POST http://localhost:8083/api/combined/carts/{cartId}/checkout
```

## Summary

The combined approach gives you the best of both worlds:

> **Onion** tells you HOW to organize layers and WHERE dependencies point.  
> **Hexagonal** tells you HOW to communicate across boundaries and WHAT to make pluggable.

Together, they create a robust architecture that is testable, maintainable, and technology-agnostic at its core.
