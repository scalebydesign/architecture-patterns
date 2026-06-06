# Rich Domain Model

## What is it?

A Rich Domain Model is a domain modeling approach where entities contain **both data AND behavior**. Business rules, validations, and state transitions are encapsulated inside the domain objects rather than scattered across service classes.

The term was coined by Martin Fowler (Patterns of Enterprise Application Architecture, 2002) as the opposite of the Anemic Domain Model anti-pattern.

## Core Principles

1. **Encapsulation** — Entity controls access to its own state via methods, not setters
2. **Self-validation** — Entity can never exist in an invalid state
3. **Behavior co-location** — Logic lives with the data it operates on
4. **Tell, Don't Ask** — You tell the entity to do something, not ask for data and do it yourself

## The Pattern in Code

```
┌─────────────────────────────────────────────────┐
│                Order (Entity)                   │
│                                                 │
│  State:                                         │
│  - id, customerId, items, status, createdAt     │
│                                                 │
│  Behavior:                                      │
│  - addItem()    → validates state, adds item    │
│  - submit()     → validates not empty, changes  │
│  - approve()    → validates is submitted        │
│  - complete()   → validates is approved         │
│  - cancel()     → validates not completed       │
│  - calculateTotal() → domain calculation        │
│                                                 │
│  Invariants enforced:                           │
│  - Cannot add items after submission            │
│  - Cannot submit empty order                    │
│  - State machine: DRAFT→SUBMITTED→APPROVED→...  │
│  - Cannot cancel completed order                │
└─────────────────────────────────────────────────┘
```


## Project Structure

```
rich-domain/
└── src/main/java/com/scalebydesign/richdomain/
    ├── RichDomainApplication.java
    └── domain/
        ├── Order.java          ← Entity with all business behavior
        ├── OrderStatus.java    ← State machine enum
        └── LineItem.java       ← Value Object with behavior
```

## Key Design Decisions

### 1. No public setters for state-transitioning fields

```java
// ❌ WRONG — allows anyone to put entity in invalid state
public void setStatus(OrderStatus status) { this.status = status; }

// ✅ CORRECT — controlled transition with validation
public void submit() {
    assertState(OrderStatus.DRAFT, "Only draft orders can be submitted");
    if (items.isEmpty()) throw new IllegalStateException("Cannot submit empty order");
    this.status = OrderStatus.SUBMITTED;
}
```

The only `setX()` methods are for persistence reconstruction (and should ideally be package-private or use a builder).

### 2. Constructor enforces initial invariants

```java
public Order(String customerId) {
    if (customerId == null || customerId.isBlank()) {
        throw new IllegalArgumentException("Customer ID is required");
    }
    this.id = UUID.randomUUID();
    this.customerId = customerId;
    this.items = new ArrayList<>();
    this.status = OrderStatus.DRAFT;  // Valid initial state
    this.createdAt = LocalDateTime.now();
}
```

There's no `new Order()` with no args. You MUST provide a customer. The entity starts in a valid state.

### 3. State machine pattern

```
DRAFT ──submit()──→ SUBMITTED ──approve()──→ APPROVED ──complete()──→ COMPLETED
  │                                                                       ↗
  └──────────────────── cancel() ────────────────────────────────────────┘
  (from any state except COMPLETED)
```

Each transition method:
- Checks the current state
- Throws if invalid
- Sets the new state

### 4. Value Objects have behavior too

```java
public class LineItem {
    public void increaseQuantity(int amount) {
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive");
        this.quantity += amount;
    }

    public BigDecimal getSubtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
```

Even line items protect their own invariants.

### 5. Defensive copies for collections

```java
public List<LineItem> getItems() { return List.copyOf(items); }
```

Returns an immutable copy. External code cannot add/remove items bypassing business rules.

## Where Logic Lives — The Decision Framework

| Question | If YES → | If NO → |
|----------|----------|---------|
| Does it only need the entity's own data? | Entity | Service |
| Does it protect the entity's invariants? | Entity | — |
| Does it need data from other entities? | Domain Service | — |
| Does it need external systems (DB, API)? | Application Service | — |
| Is it a formatting/presentation concern? | Adapter/Presenter | — |

## Testing a Rich Domain Model

The biggest benefit — testing is trivial:

```java
@Test
void cannotSubmitEmptyOrder() {
    Order order = new Order("customer-1");
    
    assertThrows(IllegalStateException.class, () -> order.submit());
}

@Test
void cannotAddItemsAfterSubmission() {
    Order order = new Order("customer-1");
    order.addItem("p1", "Widget", 2, new BigDecimal("10.00"));
    order.submit();
    
    assertThrows(IllegalStateException.class, 
        () -> order.addItem("p2", "Gadget", 1, new BigDecimal("20.00")));
}
```

No mocks. No Spring context. No database. Just `new Order()` and assert.

## When to Use Rich Domain Model

### ✅ Use when:
- Entity has genuine business rules (state machines, calculations, validations)
- Multiple code paths can reach the entity (REST, queue, CLI, scheduled jobs)
- The domain IS your competitive advantage
- You want strong guarantees that invariants are never violated
- You want simple, fast unit tests

### ❌ Avoid when:
- Entity is pure CRUD (name, email, createdAt — no rules)
- Business logic is trivial (just "save to DB")
- Team is unfamiliar with OOP design
- Framework fights against rich models (some ORMs want plain beans)

## Rich vs Anemic — Quick Comparison

| Aspect | Rich (this module) | Anemic (`anemic-domain/`) |
|--------|-------------------|---------------------------|
| `order.submit()` | Validates + transitions state | Does nothing — entity has no behavior |
| Who enforces invariants? | Entity itself | Service (and hope nobody bypasses it) |
| Testing | `new Order()` + assert | Need to test Service + mock repo |
| Discoverability | Open entity → see all business rules | Hunt across service classes |
| Encapsulation | Strong — can't bypass | Weak — `setStatus()` is public |
| Complexity | Higher learning curve | Simpler initial development |

## Common Misconceptions

**"Rich domain means no services at all"** — Wrong. You still need:
- Application Services (for orchestration, transactions, external calls)
- Domain Services (for cross-aggregate logic)

The entity handles self-contained invariants. Services handle coordination.

**"Rich domain means everything is in one file"** — Wrong. Complex entities decompose into Value Objects, inner classes, and strategy patterns.

**"Rich domain is always better"** — Wrong. For a simple `UserProfile` with just name/email/avatar, forcing behavior into it is over-engineering.
