# Event Sourcing

## What is it?

Event Sourcing is a pattern where **application state is stored as a sequence of immutable events** rather than as the current state itself. Instead of updating a row in a database, you append a new event. The current state is reconstructed by replaying all events from the beginning.

Coined and popularized by Greg Young and Martin Fowler. Commonly used in financial systems, audit-heavy domains, and event-driven architectures.

## Core Idea

```
Traditional (State Sourcing):
┌──────────────────────────────────────────┐
│  orders table                            │
│  id=1, status="SUBMITTED", total=59.98   │  ← Only current state
└──────────────────────────────────────────┘
  History? Lost. Why is it submitted? Who added items? When?


Event Sourcing:
┌──────────────────────────────────────────────────────────────┐
│  Event Stream: order-1                                       │
│                                                              │
│  [1] OrderCreated    { customerId: "c1" }         @ 10:00   │
│  [2] ItemAdded       { productId: "p1", qty: 2 }  @ 10:01   │
│  [3] ItemAdded       { productId: "p2", qty: 1 }  @ 10:02   │
│  [4] OrderSubmitted  {}                           @ 10:05   │
└──────────────────────────────────────────────────────────────┘
  Current state? Replay events 1→4. Full history preserved.
```


## The Pattern Flow

```
┌──────────────────────────────────────────────────────────────────┐
│                      EVENT SOURCING FLOW                          │
│                                                                  │
│  1. COMMAND arrives:  "Submit Order #123"                        │
│          │                                                       │
│          ▼                                                       │
│  2. LOAD aggregate from Event Store:                             │
│     - Fetch all events for order-123                             │
│     - Replay them to rebuild current state                       │
│          │                                                       │
│          ▼                                                       │
│  3. EXECUTE business logic:                                      │
│     - Validate (is order in DRAFT state? has items?)             │
│     - Produce NEW event: OrderSubmitted                          │
│          │                                                       │
│          ▼                                                       │
│  4. APPEND new event to Event Store:                             │
│     - Events are NEVER modified/deleted. Only appended.          │
│          │                                                       │
│          ▼                                                       │
│  5. (Optional) UPDATE read model / projections                   │
└──────────────────────────────────────────────────────────────────┘
```

## Project Structure

```
event-sourcing/
└── src/main/java/com/scalebydesign/eventsourcing/
    ├── EventSourcingApplication.java
    │
    ├── event/                           ← DOMAIN EVENTS (the source of truth)
    │   └── OrderEvent.java             ← Sealed interface + record subtypes
    │
    ├── aggregate/                       ← AGGREGATE (state rebuilt from events)
    │   └── Order.java                  ← Commands produce events, events update state
    │
    ├── store/                           ← EVENT STORE (append-only persistence)
    │   └── EventStore.java             ← In-memory implementation
    │
    └── service/                         ← ORCHESTRATION
        └── OrderService.java           ← Load, execute, save pattern
```

## Key Concepts in Detail

### 1. Events — Immutable facts that happened

```java
public sealed interface OrderEvent {
    UUID orderId();
    LocalDateTime occurredAt();

    record OrderCreated(UUID orderId, String customerId, LocalDateTime occurredAt) implements OrderEvent {}
    record ItemAdded(UUID orderId, String productId, String name, int quantity, BigDecimal unitPrice, LocalDateTime occurredAt) implements OrderEvent {}
    record OrderSubmitted(UUID orderId, LocalDateTime occurredAt) implements OrderEvent {}
    // ...
}
```

Events are:
- **Past tense** — "OrderCreated", not "CreateOrder" (that's a command)
- **Immutable** — once stored, never changed
- **Append-only** — never deleted
- **Self-contained** — carry all data needed to reconstruct state
- **Sealed** — Java sealed interface ensures exhaustive handling

### 2. Aggregate — State rebuilt by replaying events

The aggregate has two paths for events:

```java
// PATH 1: New command → validate → produce event → apply to self
public void addItem(String productId, String name, int quantity, BigDecimal unitPrice) {
    if (status != OrderStatus.DRAFT) throw new IllegalStateException("...");
    apply(new ItemAdded(id, productId, name, quantity, unitPrice, LocalDateTime.now()));
}

// PATH 2: Replaying from history → just apply (no validation — it already happened)
public void replay(OrderEvent event) {
    handle(event);  // Same state change, no validation
}

// Shared state mutation
private void handle(OrderEvent event) {
    switch (event) {
        case OrderCreated e -> { this.id = e.orderId(); this.status = OrderStatus.DRAFT; }
        case ItemAdded e -> { items.put(e.productId(), new LineItem(...)); }
        case OrderSubmitted e -> { this.status = OrderStatus.SUBMITTED; }
        // ...
    }
}
```

**Why two paths?**
- `apply()` — validates business rules THEN produces event (new commands)
- `replay()` — just applies state (historical events already validated when they happened)

### 3. Uncommitted Events — Transaction boundary

```java
private final List<OrderEvent> uncommittedEvents = new ArrayList<>();

private void apply(OrderEvent event) {
    handle(event);                    // Update in-memory state
    uncommittedEvents.add(event);    // Track for persistence
}
```

After the service saves:
```java
private void save(Order order) {
    List<OrderEvent> newEvents = order.getUncommittedEvents();
    eventStore.append(order.getId(), newEvents);  // Persist
    order.clearUncommittedEvents();               // Reset
}
```

### 4. Event Store — Append-only storage

```java
public void append(UUID aggregateId, List<OrderEvent> events) {
    streams.computeIfAbsent(aggregateId, k -> new ArrayList<>()).addAll(events);
    // NEVER: update, delete, or modify existing events
}

public List<OrderEvent> getEvents(UUID aggregateId) {
    return streams.getOrDefault(aggregateId, List.of());
}
```

In production, this would be:
- **EventStoreDB** — purpose-built event database (Greg Young)
- **PostgreSQL** — events table with aggregate_id, version, event_type, payload (JSONB)
- **Kafka** — each aggregate has a topic partition
- **DynamoDB** — partition key = aggregate ID, sort key = version

## Time Travel — The Killer Feature

```java
// Get current state:
List<OrderEvent> allEvents = eventStore.getEvents(orderId);
Order current = new Order();
allEvents.forEach(current::replay);

// Get state at any point in time:
Order atEvent3 = new Order();
allEvents.stream().limit(3).forEach(atEvent3::replay);
// → Shows state BEFORE submission (only created + 2 items added)
```

This is impossible with traditional state storage. You'd need manual audit tables.

## Snapshotting — Performance Optimization

Problem: Aggregate with 10,000 events takes 10,000 replays to load.

Solution: Periodically save a "snapshot" of current state:

```
Events: [1] [2] [3] ... [5000] [SNAPSHOT @ 5000] [5001] [5002] ... [5050]

To load: Start from SNAPSHOT, replay only events 5001→5050 (50 events, not 5050)
```

Not implemented in our example (educational simplicity) but critical in production.

## Production Event Store Schema (PostgreSQL)

```sql
CREATE TABLE events (
    id              BIGSERIAL PRIMARY KEY,
    aggregate_id    UUID NOT NULL,
    aggregate_type  VARCHAR(100) NOT NULL,
    event_type      VARCHAR(100) NOT NULL,
    version         INT NOT NULL,
    payload         JSONB NOT NULL,
    metadata        JSONB,
    occurred_at     TIMESTAMP NOT NULL,
    
    UNIQUE (aggregate_id, version)  -- Optimistic concurrency
);

CREATE INDEX idx_events_aggregate ON events(aggregate_id, version);
```

The `version` + unique constraint gives you **optimistic concurrency** — two concurrent writes to the same aggregate will conflict on version number.

## Event Sourcing vs State Sourcing

| Aspect | State Sourcing (traditional) | Event Sourcing |
|--------|------------------------------|---------------|
| Storage | Current state only | All events (history) |
| Audit trail | Manual (audit tables) | Built-in (events ARE the audit) |
| Debugging | "Why is status FAILED?" → unknown | Replay events → see exactly what happened |
| Temporal queries | Not possible | "What was the state on March 15?" → replay |
| Schema migration | ALTER TABLE (risky) | Events are immutable; add new event types |
| Storage size | Compact | Grows over time (mitigated by snapshots) |
| Consistency | Strong (one row) | Strong per aggregate (events are sequential) |
| Complexity | Low | High (replay, versioning, snapshots) |

## When to Use Event Sourcing

### ✅ Use when:
- **Audit is mandatory** — finance, healthcare, legal compliance
- **Temporal queries needed** — "what was the balance on Dec 31?"
- **Complex domain** — state machines with many transitions
- **Event-driven architecture** — events feed other services naturally
- **Debugging is critical** — reproducing production issues from event replay
- **Undo/redo needed** — compensating events reverse actions
- **Analytics from history** — "how many orders were cancelled after being approved?"

### ❌ Avoid when:
- Simple CRUD with no audit requirements
- Performance-critical reads (unless combined with CQRS projections)
- Team is unfamiliar with eventual consistency
- Domain has no meaningful state transitions
- Data privacy regulations require deletion (GDPR right to erasure conflicts with append-only)

## Relationship with CQRS

Event Sourcing and CQRS are independent but **synergistic**:

- **Event Sourcing without CQRS** — possible but reads are slow (rebuild from events every time)
- **CQRS without Event Sourcing** — common (separate read/write stores, no event history)
- **Event Sourcing + CQRS** — ideal combination:
  - Write side: events → event store
  - Read side: event handlers build projections (read models)
  - Events naturally connect the two

## Frameworks and Tools

| Tool | What it does |
|------|-------------|
| **EventStoreDB** | Purpose-built event database (Greg Young's product) |
| **Axon Framework** | Java framework for CQRS + Event Sourcing (annotations) |
| **Marten** | .NET library for Event Sourcing on PostgreSQL |
| **Apache Kafka** | Event streaming platform (can serve as event store) |
| **Debezium** | CDC tool — captures DB changes as events |

## Common Pitfalls

1. **Event versioning** — Events are immutable, but your code evolves. Use upcasters to handle old event formats.
2. **Large aggregates** — Thousands of events per aggregate → use snapshotting.
3. **GDPR/deletion** — Append-only conflicts with "right to be forgotten." Use crypto-shredding (encrypt PII per user, delete the key).
4. **Eventual consistency anxiety** — If using async projections, reads lag behind writes. Design UIs accordingly.
5. **Over-eventing** — Not everything needs events. If "UserEmailChanged" has no downstream consumers, just update the field.
