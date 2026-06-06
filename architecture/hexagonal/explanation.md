# Hexagonal Architecture (Ports & Adapters)

## What is it?

Hexagonal Architecture, coined by Alistair Cockburn in 2005, structures an application so that the **core business logic is completely isolated** from the outside world. The outside world communicates with the application through well-defined **Ports** (interfaces), and concrete implementations called **Adapters** plug into those ports.

## Core Idea

Imagine your application as a hexagon (the shape doesn't matter — it's just a metaphor for "multiple sides"). Each side represents a way the application can interact with the outside world.

```
                    ┌─────────────────────┐
                    │   REST Controller   │  ← Driving Adapter
                    └──────────┬──────────┘
                               │ calls
                    ┌──────────▼──────────┐
                    │   Inbound Port      │  ← Interface (OrderUseCase)
                    │   (Driving Port)    │
                    └──────────┬──────────┘
                               │ implemented by
                    ┌──────────▼──────────┐
                    │                     │
                    │   APPLICATION       │  ← OrderService
                    │   SERVICE           │
                    │                     │
                    └──────────┬──────────┘
                               │ uses
                    ┌──────────▼──────────┐
                    │   Outbound Port     │  ← Interface (OrderRepository, PaymentGateway)
                    │   (Driven Port)     │
                    └──────────┬──────────┘
                               │ implemented by
                    ┌──────────▼──────────┐
                    │  JPA Repository /   │  ← Driven Adapters
                    │  Stripe Adapter     │
                    └─────────────────────┘
```


## Key Terminology

### Ports
Interfaces defined by the application core that describe **what** the application can do (inbound) or **what** it needs (outbound).

| Port Type | Also Called | Direction | Example |
|-----------|-------------|-----------|---------|
| Inbound Port | Driving Port | Outside → Application | `OrderUseCase` |
| Outbound Port | Driven Port | Application → Outside | `OrderRepository`, `PaymentGateway` |

### Adapters
Concrete implementations that plug into ports.

| Adapter Type | Also Called | What it does | Example |
|--------------|-------------|--------------|---------|
| Inbound Adapter | Driving Adapter | Translates external input (HTTP, CLI, events) into port calls | `OrderController` |
| Outbound Adapter | Driven Adapter | Implements port using infrastructure (DB, APIs) | `OrderJpaRepository`, `StripePaymentAdapter` |

## The Dependency Rule

**Dependencies always point INWARD.**

```
Adapters → Ports → Domain
```

- Adapters depend on Ports (interfaces).
- Application Service implements inbound ports and uses outbound ports.
- Domain model has ZERO external dependencies.
- Ports are defined in the domain, not in the adapters.


## Project Structure Mapping

```
hexagonal/
├── domain/                          ← THE CORE (center of hexagon)
│   ├── model/
│   │   ├── Order.java              ← Entity with business rules
│   │   ├── OrderItem.java          ← Value Object
│   │   └── OrderStatus.java        ← Enum
│   └── port/
│       ├── inbound/
│       │   └── OrderUseCase.java   ← What can the outside world DO? (Driving Port)
│       └── outbound/
│           ├── OrderRepository.java ← What does the domain NEED? (Driven Port)
│           └── PaymentGateway.java  ← What does the domain NEED? (Driven Port)
│
├── application/
│   └── OrderService.java           ← Implements inbound port, uses outbound ports
│
└── adapter/
    ├── inbound/
    │   └── OrderController.java    ← HTTP → OrderUseCase (Driving Adapter)
    └── outbound/
        ├── OrderJpaRepository.java ← OrderRepository → JPA (Driven Adapter)
        ├── OrderJpaEntity.java     ← Infrastructure model (not domain!)
        └── StripePaymentAdapter.java ← PaymentGateway → Stripe (Driven Adapter)
```

## How it works in this example

### 1. A request comes in (Driving side)
```
HTTP POST /api/hexagonal/orders/{id}/pay
    → OrderController (Driving Adapter)
        → calls OrderUseCase.payOrder() (Inbound Port)
```

### 2. Application Service orchestrates
```
OrderService.payOrder()
    → finds order via OrderRepository (Outbound Port)
    → calls PaymentGateway.processPayment() (Outbound Port)
    → calls order.markPaid() (Domain Logic)
    → saves via OrderRepository (Outbound Port)
```

### 3. Adapters handle the infrastructure
```
OrderRepository.findById()
    → OrderJpaRepository translates domain ↔ JPA entity
    → Spring Data JPA talks to H2 database

PaymentGateway.processPayment()
    → StripePaymentAdapter calls Stripe API (simulated)
```


## Why use Hexagonal Architecture?

### ✅ Benefits
1. **Testability** — Mock any port to unit test the domain without a database or HTTP server.
2. **Swappability** — Replace Stripe with PayPal? Just write a new adapter. Domain untouched.
3. **Framework independence** — Spring is in adapters only. Domain is pure Java.
4. **Multiple entry points** — Same use case can be driven by REST, CLI, message queue, scheduled job.
5. **Clear boundaries** — Ports make the application's contract explicit.

### ❌ When NOT to use
1. Simple CRUD applications with no business logic.
2. Rapid prototypes where speed matters more than maintainability.
3. Very small microservices that will never grow.

## Key Insight

> The domain defines the INTERFACES (ports).  
> The infrastructure provides the IMPLEMENTATIONS (adapters).  
> This is the **Dependency Inversion Principle** applied architecturally.

The domain never says "save to PostgreSQL." It says "I need something that can save an Order" — and the adapter decides how.

## Running this module

```bash
./gradlew :hexagonal:bootRun
```

Try the API:
```bash
# Create order
curl -X POST http://localhost:8081/api/hexagonal/orders \
  -H "Content-Type: application/json" \
  -d '{"customerId": "cust-123"}'

# Add item
curl -X POST http://localhost:8081/api/hexagonal/orders/{orderId}/items \
  -H "Content-Type: application/json" \
  -d '{"productId": "prod-1", "productName": "Laptop", "quantity": 1, "price": 999.99}'

# Confirm and pay
curl -X POST http://localhost:8081/api/hexagonal/orders/{orderId}/confirm
curl -X POST http://localhost:8081/api/hexagonal/orders/{orderId}/pay
```


---

## FAQ: Why is business logic in the Entity, not in the Service?

You might notice that `Order.java` has methods like `addItem()`, `confirm()`, `markPaid()`, and `cancel()`. Why isn't this in the `OrderService`?

### The Rich Domain Model principle

This follows DDD's **Rich Domain Model** — the entity protects its own invariants.

```java
// Logic that guards the entity's OWN state lives IN the entity
public void addItem(...) {
    if (status != OrderStatus.CREATED) {
        throw new InvalidOrderStateException("Cannot add items after confirmation");
    }
    items.add(new OrderItem(...));
}
```

Only the Order knows its current state. Only the Order should decide whether it's valid to add an item. This is **encapsulation** — the most fundamental OOP principle.

### When does logic go WHERE?

| Logic type | Where it lives | Why | Example |
|---|---|---|---|
| Self-contained invariant | **Entity** | Only needs data the entity already has | `order.confirm()` — checks if items exist |
| Cross-entity operation | **Domain Service** | Needs data from multiple aggregates | `cartDomainService.calculateDiscountedTotal()` |
| External system orchestration | **Application Service** | Needs outbound ports (DB, APIs) | `orderService.payOrder()` — calls PaymentGateway |

### The alternative: Anemic Domain Model (anti-pattern)

If you move ALL logic to the service:

```java
// ANEMIC — entity is just a data bag
public class Order {
    private UUID id;
    private List<OrderItem> items;
    private OrderStatus status;
    // Only getters/setters. No behavior.
}

// Everything in service — procedural code disguised as OOP
public class OrderService {
    public void addItem(Order order, ...) {
        if (order.getStatus() != CREATED) throw ...;  // Who enforces this?
        order.getItems().add(...);  // Anyone can bypass this check
    }
}
```

**Problems with this:**
- Nothing prevents bypassing the service and mutating the entity directly
- Business rules scatter across multiple service classes
- The entity can't protect itself — anyone can put it in an invalid state
- You lose the benefit of encapsulation

### The guideline for complex logic

> "If it's complex but only needs the entity's own data → it still belongs in the entity."
> "If it needs external data or coordinates between entities → move to a service."

**Complex logic in entity — still fine:**
```java
public BigDecimal calculateTotal() {
    return items.stream()
            .map(OrderItem::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
}
```

**Complex logic needing external data — goes to service:**
```java
// Application Service — needs PaymentGateway (external system)
public Order payOrder(UUID orderId) {
    Order order = repository.findById(orderId);
    boolean success = paymentGateway.processPayment(order.getCustomerId(), order.calculateTotal());
    if (success) order.markPaid();  // Entity method still enforces the state transition
    return repository.save(order);
}
```

Notice: Even in the service, `order.markPaid()` still validates the state transition. The entity ALWAYS guards its own invariants.


---

## FAQ: How do validations work in a Rich Domain Model?

### The Value Object pattern

In DDD, things like email, phone number, or money aren't plain Strings — they're **Value Objects**. A Value Object validates itself on creation and can never exist in an invalid state.

```java
// Value Object — validates itself. If you can hold a reference to it, it's valid.
public class Email {

    private final String value;

    public Email(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidEmailException("Email cannot be empty");
        }
        if (!value.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            throw new InvalidEmailException("Invalid email format: " + value);
        }
        this.value = value.toLowerCase().trim();
    }

    public String getValue() { return value; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Email email)) return false;
        return value.equals(email.value);
    }

    @Override
    public int hashCode() { return value.hashCode(); }
}
```

The entity then uses the Value Object:

```java
public class Customer {
    private final Email email;        // Not String — the type guarantees validity
    private final String name;

    public Customer(String name, String email) {
        this.name = name;
        this.email = new Email(email);  // Validated at construction time
    }
}
```

### Why Value Objects beat service-level validation

| Approach | Problem |
|----------|---------|
| Validate in controller | Domain can receive invalid emails from queues, CLI, or other services |
| Validate in service | Someone can construct `Customer` without going through the service |
| Validate in entity (as String) | Validation clutters the entity, gets duplicated |
| **Value Object** | **Impossible to have an invalid email anywhere in the system** |

Key insight:

> If you can hold a reference to an `Email` object, it's guaranteed valid. The type system enforces it. No need to re-validate anywhere else.

### Where different types of validation live

| Validation type | Where | Example |
|---|---|---|
| Format / structural | **Value Object** | `Email`, `PhoneNumber`, `Money`, `OrderId` |
| Entity state invariants | **Entity** | `order.confirm()` — checks items not empty |
| Cross-entity rules | **Domain Service** | "Customer can't have more than 5 active orders" |
| Input sanitization | **Adapter (controller/DTO)** | Trim whitespace, check JSON has required fields |
| Uniqueness checks | **Application Service** | "Email not already registered" (needs DB query) |

### The layered validation flow

```
HTTP Request
    │
    ▼
Controller / DTO ─── "Is JSON well-formed? Required fields present?"
    │                (adapter concern — reject garbage early)
    ▼
Application Service ─── "Is this email unique in the system?"
    │                    (needs repository — can't live in entity)
    ▼
Entity / Value Object ─── "Is this a valid email format?"
    │                      "Can this order be confirmed?"
    │                      (self-contained invariants — last line of defense)
    ▼
Saved to database
```

Each layer validates what it's responsible for. The domain (entity + value objects) is the **last line of defense** — even if all other layers are bypassed, the model can never be in an invalid state.

### Complex validation that needs external data

When validation requires information beyond the entity's own data, use a Domain Service:

```java
// Domain Service — complex cross-entity validation
public class OrderValidationService {

    public void validateOrderPlacement(Order order, Customer customer, InventoryStatus inventory) {
        if (customer.isBlacklisted()) 
            throw new CustomerBlockedException(customer.getId());
        if (!inventory.hasStock(order.getItems())) 
            throw new InsufficientInventoryException(...);
        if (customer.getActiveOrderCount() >= 5) 
            throw new OrderLimitExceededException(customer.getId());
    }
}
```

This lives in a Domain Service because it needs data from multiple aggregates. The entity still handles its OWN invariants internally — the service handles invariants that SPAN entities.

### Summary

- **Simple format validation** → Value Object (self-validating, immutable)
- **Entity state guards** → Entity methods (protects its own invariants)
- **Cross-entity business rules** → Domain Service
- **External uniqueness/existence checks** → Application Service (needs infrastructure)
- **Input shape/format** → Adapter layer (reject bad requests early)
