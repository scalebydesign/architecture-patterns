# Anemic Domain Model

## What is it?

An Anemic Domain Model is a domain modeling approach where entities are **pure data containers** (getters/setters only) with **zero business logic**. All behavior, validation, and state transitions live in service classes.

Martin Fowler called this an "anti-pattern" in 2003, but it remains the most common pattern in enterprise Java development — especially in CRUD-heavy applications.

## Core Characteristics

1. **Entity = data bag** — only fields, getters, and setters
2. **Service = all logic** — validation, transitions, calculations
3. **No encapsulation** — entity state is publicly mutable
4. **Procedural in OOP clothing** — looks like OOP but behaves like procedural code

## The Pattern in Code

```
┌─────────────────────────────────┐     ┌─────────────────────────────────┐
│         Order (Entity)          │     │       OrderService               │
│                                 │     │                                 │
│  - id: UUID                     │     │  createOrder()                  │
│  - customerId: String           │     │  addItem()                      │
│  - items: List<LineItem>        │     │  submitOrder()                  │
│  - status: String               │     │  approveOrder()                 │
│  - total: BigDecimal            │     │  completeOrder()                │
│                                 │     │  cancelOrder()                  │
│  + getters                      │     │  recalculateTotal()             │
│  + setters                      │     │                                 │
│  (no business logic)            │     │  (ALL business logic here)      │
└─────────────────────────────────┘     └─────────────────────────────────┘
```


## Project Structure

```
anemic-domain/
└── src/main/java/com/scalebydesign/anemicdomain/
    ├── AnemicDomainApplication.java
    ├── model/
    │   ├── Order.java              ← Pure data bag (no behavior)
    │   └── LineItem.java           ← Pure data bag (no behavior)
    └── service/
        └── OrderService.java       ← ALL business logic here
```

## Key Design Decisions (and their consequences)

### 1. Entity has public setters — no protection

```java
public class Order {
    private String status;

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    // Anyone can call setStatus("COMPLETED") without validation
}
```

**Consequence:** Nothing prevents bypassing the service:
```java
// This compiles and runs — but violates business rules
Order order = orderService.getOrder(id);
order.setStatus("COMPLETED");  // Skipped all validation!
orderRepository.save(order);
```

### 2. All validation is in the service

```java
@Service
public class OrderService {
    public Order submitOrder(UUID orderId) {
        Order order = findOrder(orderId);

        // Validation HERE, not in entity
        if (!"DRAFT".equals(order.getStatus())) {
            throw new IllegalStateException("Only draft orders can be submitted");
        }
        if (order.getItems().isEmpty()) {
            throw new IllegalStateException("Cannot submit an empty order");
        }

        order.setStatus("SUBMITTED");  // Direct mutation
        return order;
    }
}
```

**Consequence:** If another service, job, or code path also manipulates Orders, they must duplicate this validation or risk inconsistency.

### 3. Status is a String, not an enum enforced by the entity

```java
private String status;  // Could be "DRAFT", "draft", "SHIPPED", "oops", anything
```

In the Rich model, the entity uses an `enum` and transition methods that make illegal states unrepresentable.

### 4. Total is stored, not calculated

```java
private BigDecimal total;

// Recalculated manually in the service
private void recalculateTotal(Order order) {
    BigDecimal total = order.getItems().stream()
            .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    order.setTotal(total);
}
```

**Consequence:** If someone adds an item without calling `recalculateTotal()`, the total is stale. In the Rich model, `calculateTotal()` always computes from source data — it can never be stale.

## Why It's Called an "Anti-Pattern"

Martin Fowler's argument (2003):

> "The fundamental horror of this anti-pattern is that it's so contrary to the basic idea of object-oriented design; which is to combine data and process together."

The problems:

| Problem | Explanation |
|---------|-------------|
| **No encapsulation** | Entity state is publicly mutable; invariants can be violated |
| **Scattered logic** | Multiple services might contain rules about the same entity |
| **Duplication risk** | Same validation repeated in different services |
| **False OOP** | Looks like objects but behaves like C structs + functions |
| **Testing requires mocks** | Service tests need a mocked repository; entity tests are pointless (nothing to test) |

## Why It's Still Extremely Common

Despite being labeled an anti-pattern, it dominates enterprise Java. Reasons:

1. **Framework alignment** — JPA/Hibernate want plain beans with getters/setters
2. **Code generators** — Tools like Lombok, MapStruct generate anemic models
3. **Familiarity** — Most tutorials teach Controller → Service → Repository with dumb entities
4. **CRUD dominance** — Many apps are genuinely just data in/out with thin logic
5. **Team skill** — Rich models need DDD knowledge; anemic is immediately understandable
6. **Serialization** — JSON/XML mappers work best with plain getters/setters
7. **Inertia** — "That's how we've always done it"

## When Anemic is Actually Fine

| Scenario | Why anemic works |
|----------|-----------------|
| Simple CRUD REST API | No real business rules — just persist and retrieve |
| Backend-for-frontend (BFF) | Just reshaping data between services and UI |
| Reporting/read-heavy services | No mutations — just queries |
| Rapid prototyping / MVP | Speed matters more than architectural purity |
| Data pipeline services | Transform and forward — no domain logic |

## Comparison: Same Operation in Rich vs Anemic

### Submit Order

**Rich Domain:**
```java
// Caller
order.submit();  // One line. Entity validates internally.

// Inside Order.java
public void submit() {
    assertState(OrderStatus.DRAFT, "Only draft orders can be submitted");
    if (items.isEmpty()) throw new IllegalStateException("Cannot submit empty order");
    this.status = OrderStatus.SUBMITTED;
}
```

**Anemic Domain:**
```java
// In OrderService.java
public Order submitOrder(UUID orderId) {
    Order order = findOrder(orderId);
    if (!"DRAFT".equals(order.getStatus())) {
        throw new IllegalStateException("Only draft orders can be submitted");
    }
    if (order.getItems().isEmpty()) {
        throw new IllegalStateException("Cannot submit an empty order");
    }
    order.setStatus("SUBMITTED");
    return order;
}
```

Same logic, different location. The Rich version can't be bypassed. The Anemic version relies on everyone going through the service.

## The Spectrum (Not Binary)

Most real codebases aren't purely Rich or purely Anemic. They exist on a spectrum:

```
Pure Anemic          Pragmatic Hybrid           Pure Rich
│                          │                          │
│  Entity = only data      │  Entity has some         │  Entity has all
│  Service = all logic     │  validation + simple     │  business rules
│                          │  state guards.           │  Service = only
│                          │  Service handles         │  orchestration
│                          │  complex/external logic  │
▼                          ▼                          ▼
CRUD apps                 Most real apps             DDD/complex domains
```

## Honest Assessment

| If your app... | Use |
|----------------|-----|
| Has < 5 business rules per entity | Anemic is fine |
| Has complex state machines | Rich is better |
| Is maintained by 1-2 developers | Anemic is pragmatic |
| Is maintained by large teams touching same entities | Rich prevents bugs |
| Will live 6+ months and grow | Start Rich or migrate later |
| Is a throwaway prototype | Anemic is faster to write |
