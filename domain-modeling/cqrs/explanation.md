# CQRS (Command Query Responsibility Segregation)

## What is it?

CQRS separates the **read model** (queries) from the **write model** (commands) into two distinct paths. Instead of a single model that handles both reads and writes, you maintain:

- **Write side** — Rich domain model optimized for enforcing business rules
- **Read side** — Flat, denormalized model optimized for fast queries

The term was coined by Greg Young (2010), building on Bertrand Meyer's CQS (Command-Query Separation) principle.

## Why Separate Reads and Writes?

In most applications:
- **Reads vastly outnumber writes** (often 90/10 or 99/1)
- **Read and write models have different shapes** — write needs nested aggregates; read needs flat lists
- **Read and write have different scaling needs** — reads can be cached/replicated; writes need consistency
- **Optimization conflicts** — indexes that speed reads slow writes and vice versa

```
Traditional (single model):
┌──────────────┐
│   Order      │  ← Same model for BOTH reading and writing
│   (complex)  │  ← Compromises everywhere
└──────────────┘

CQRS (split):
┌──────────────────────┐          ┌──────────────────────┐
│   Write Model        │          │   Read Model         │
│   (Rich, nested,     │          │   (Flat, denorm.,    │
│    enforces rules)   │          │    fast queries)     │
└──────────────────────┘          └──────────────────────┘
```


## The Architecture

```
                    ┌─────────────┐
                    │   Client    │
                    └──────┬──────┘
                           │
              ┌────────────┴────────────┐
              │                         │
        Commands (writes)          Queries (reads)
              │                         │
              ▼                         ▼
┌─────────────────────────┐  ┌─────────────────────────┐
│   COMMAND HANDLER       │  │    QUERY HANDLER        │
│   - Validates command   │  │    - Simple data fetch  │
│   - Loads aggregate     │  │    - No business logic  │
│   - Executes domain     │  │    - Returns DTO        │
│     logic               │  │                         │
└────────────┬────────────┘  └────────────┬────────────┘
             │                             │
             ▼                             ▼
┌─────────────────────────┐  ┌─────────────────────────┐
│   WRITE MODEL           │  │    READ MODEL           │
│   (Rich Domain)         │  │    (Flat DTO)           │
│   - Order aggregate     │  │    - OrderReadModel     │
│   - LineItems           │  │    - Pre-calculated     │
│   - State machine       │  │      total, itemCount   │
│   - Business rules      │  │    - Optimized for      │
│                         │  │      listing/search     │
└────────────┬────────────┘  └────────────┬────────────┘
             │                             │
             ▼                             ▼
┌─────────────────────────┐  ┌─────────────────────────┐
│  WRITE STORE            │  │  READ STORE             │
│  (Normalized, consistent)│ │  (Denormalized, fast)   │
└─────────────────────────┘  └─────────────────────────┘
             │                             ▲
             └──────── SYNC ───────────────┘
                  (after every write,
                   update read model)
```

## Project Structure

```
cqrs/
└── src/main/java/com/scalebydesign/cqrs/
    ├── CqrsApplication.java
    │
    ├── command/                          ← WRITE SIDE
    │   ├── CreateOrderCommand.java     ← Intent to change state (imperative)
    │   ├── AddItemCommand.java
    │   ├── SubmitOrderCommand.java
    │   └── OrderCommandHandler.java    ← Processes commands, enforces rules
    │
    ├── write/                           ← WRITE MODEL
    │   ├── Order.java                  ← Rich domain aggregate
    │   └── OrderWriteRepository.java   ← Persistence for write model
    │
    ├── query/                           ← READ SIDE
    │   └── OrderQueryHandler.java      ← Handles queries, returns DTOs
    │
    └── read/                            ← READ MODEL
        ├── OrderReadModel.java         ← Flat, denormalized, pre-calculated
        └── OrderReadRepository.java    ← Persistence for read model
```

## Key Concepts

### Commands — "I want to DO something"

```java
public record CreateOrderCommand(String customerId) {}
public record AddItemCommand(UUID orderId, String productId, String name, int quantity, BigDecimal unitPrice) {}
public record SubmitOrderCommand(UUID orderId) {}
```

Commands are:
- **Imperative** — named as verbs ("Create", "Add", "Submit")
- **Immutable** — records/value objects
- **Can be rejected** — if business rules say no
- **Change state** — they mutate the write model

### Queries — "I want to KNOW something"

Queries are simple data retrieval. No business logic, no side effects.

```java
public OrderReadModel getOrder(UUID id) {
    return readRepository.findById(id).orElseThrow(...);
}
```

### Write Model — Rich, enforces rules

```java
public class Order {
    public void addItem(...) {
        if (status != OrderStatus.DRAFT) throw new IllegalStateException(...);
        items.add(new LineItem(...));
    }
    public void submit() {
        if (items.isEmpty()) throw new IllegalStateException(...);
        this.status = OrderStatus.SUBMITTED;
    }
}
```

The write model is a **Rich Domain Model**. It's optimized for correctness, not queries.

### Read Model — Flat, optimized for display

```java
public record OrderReadModel(
    UUID id,
    String customerId,
    String status,
    BigDecimal total,      // Pre-calculated! Not computed on-the-fly
    int itemCount,         // Pre-calculated!
    LocalDateTime createdAt
) {}
```

The read model is:
- **Denormalized** — pre-joined, no need for complex queries
- **Pre-calculated** — total and itemCount are computed at write time
- **Flat** — no nested objects, perfect for list views
- **Disposable** — can be rebuilt from the write side at any time

### Sync — Keeping read model updated

```java
private void syncReadModel(Order order) {
    OrderReadModel readModel = new OrderReadModel(
        order.getId(),
        order.getCustomerId(),
        order.getStatus().name(),
        order.calculateTotal(),
        order.getTotalItemCount(),
        order.getCreatedAt()
    );
    readRepository.save(readModel);
}
```

In our example, sync is **synchronous** (happens in the same transaction). In production, it's often **asynchronous** via events — introducing eventual consistency.

## Synchronous vs Asynchronous CQRS

| Aspect | Sync (our example) | Async (production) |
|--------|-------------------|-------------------|
| Consistency | Strong — read is always up-to-date | Eventual — read may lag |
| Complexity | Low | High (events, queues, retry logic) |
| Performance | Moderate | High read throughput |
| Implementation | Same transaction | Events via Kafka/RabbitMQ |
| When to use | Starting out, low traffic | High read traffic, scaling |

## When to Use CQRS

### ✅ Use when:
- Read/write ratio is heavily skewed (90%+ reads)
- Read and write shapes are fundamentally different
- You need different scaling strategies for reads vs writes
- Complex queries slow down due to normalized write schema
- Multiple read views of the same data (mobile view, admin view, report view)
- Combined with Event Sourcing (natural fit)

### ❌ Avoid when:
- Simple CRUD where read = write shape
- Low traffic — overhead isn't justified
- Team isn't comfortable with eventual consistency
- Single-user applications
- Read model is just "SELECT * FROM entity"

## CQRS + Event Sourcing (Common Combination)

```
Command → Write Model (aggregate) → Produces EVENTS → Event Store
                                                          │
                                                          ▼
                                                    Event Handler
                                                          │
                                                          ▼
                                                    Read Model (projection)
```

In this combination:
- The write side uses Event Sourcing (state from events)
- The read side is a "projection" rebuilt by processing events
- The event store is the single source of truth

See the `event-sourcing/` module for the write side pattern.

## Production Considerations

### Multiple Read Models

You can have MANY read models for the same write model:

```
Write: Order (aggregate)
    ├── Read: OrderListView (for grid/listing)
    ├── Read: OrderDetailView (for single order page)
    ├── Read: OrderAnalyticsView (for reporting)
    └── Read: OrderSearchView (for full-text search / Elasticsearch)
```

### Eventual Consistency Handling

When reads lag behind writes, the UI must handle it:
- Show "processing..." after commands
- Poll or use WebSocket for updates
- Display "last updated" timestamps
- Use optimistic UI patterns

### Technology Choices in Production

| Concern | Options |
|---------|---------|
| Command bus | Axon Framework, custom, Spring ApplicationEventPublisher |
| Write store | PostgreSQL, MongoDB, EventStoreDB |
| Read store | PostgreSQL, Redis, Elasticsearch, DynamoDB |
| Event transport | Kafka, RabbitMQ, AWS SNS/SQS |
| Read sync | Kafka consumer, Spring @EventListener, CDC (Debezium) |
