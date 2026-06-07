# Modular Monolith

## What is it?

A Modular Monolith is a single deployable application with **strict module boundaries**. Each module owns its data, exposes a public API (facade), and hides its internals. Modules communicate through facades (synchronous) or events (asynchronous).

It's the pragmatic middle ground between a Big Ball of Mud monolith and microservices.

## Why not just microservices?

| Concern | Microservices | Modular Monolith |
|---------|---------------|------------------|
| Deployment | Many services, complex orchestration | Single deployment |
| Data consistency | Eventual, distributed transactions | In-process, can share DB transactions |
| Network | Latency, retries, circuit breakers | In-memory method calls |
| Refactoring | Expensive (API changes across services) | Cheap (refactor within the monolith) |
| Team independence | High | Medium-High (module ownership) |
| Operational overhead | High (infra per service) | Low |

The Modular Monolith gives you the **logical separation** of microservices without the **operational complexity**.

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                     SINGLE DEPLOYABLE (Spring Boot)                  │
│                                                                     │
│  ┌────────────────┐   ┌────────────────┐   ┌────────────────────┐  │
│  │  ORDER MODULE  │   │ INVENTORY MOD  │   │ NOTIFICATION MOD   │  │
│  │                │   │                │   │                    │  │
│  │  api/          │   │  api/          │   │  api/              │  │
│  │  ├─OrderFacade │   │  ├─InvFacade   │   │  ├─NotifFacade     │  │
│  │  ├─OrderDto    │   │  └─────────────│   │  └────────────────│  │
│  │  └─OrderPlaced │   │                │   │                    │  │
│  │     Event      │   │  internal/     │   │  internal/         │  │
│  │                │   │  ├─StockRepo   │   │  ├─NotifImpl       │  │
│  │  internal/     │   │  ├─InvImpl     │   │  └─EventHandler    │  │
│  │  ├─Order       │   │  └─EventHandler│   │                    │  │
│  │  ├─OrderRepo   │   │                │   │                    │  │
│  │  └─FacadeImpl  │   └────────────────┘   └────────────────────┘  │
│  └────────────────┘            ▲                     ▲              │
│          │                     │                     │              │
│          │         ┌───────────┴─────────────────────┘              │
│          ▼         │                                                │
│  ┌─────────────────┴──────────────────────────┐                     │
│  │              SHARED KERNEL                  │                     │
│  │  EventBus, DomainEvent interface           │                     │
│  └────────────────────────────────────────────┘                     │
└─────────────────────────────────────────────────────────────────────┘
```


## Key Rules

1. **No module accesses another module's `internal` package** — only the `api` package is public
2. **Facades are the gate** — all cross-module calls go through interfaces in `api/`
3. **Events for reactions** — when a module needs to trigger side effects in other modules, it publishes an event
4. **Each module owns its data** — the Order module has its own repository, the Inventory module has its own

## Project Structure

```
modular-monolith/
├── shared/                              # Shared Kernel
│   ├── DomainEvent.java                # Event contract
│   └── EventBus.java                  # In-process event bus
│
├── order/                              # ORDER MODULE
│   ├── api/                           # ← Public (other modules can use)
│   │   ├── OrderFacade.java          #   Interface
│   │   ├── OrderDto.java             #   Response type
│   │   └── OrderPlacedEvent.java     #   Published event
│   └── internal/                      # ← Private (nobody else touches this)
│       ├── Order.java                #   Domain entity
│       ├── OrderRepository.java      #   Data access
│       └── OrderFacadeImpl.java      #   Implementation
│
├── inventory/                          # INVENTORY MODULE
│   ├── api/
│   │   └── InventoryFacade.java
│   └── internal/
│       ├── StockRepository.java
│       ├── InventoryFacadeImpl.java
│       └── InventoryEventHandler.java  # Reacts to OrderPlacedEvent
│
├── notification/                       # NOTIFICATION MODULE
│   ├── api/
│   │   └── NotificationFacade.java
│   └── internal/
│       ├── NotificationFacadeImpl.java
│       └── NotificationEventHandler.java  # Reacts to OrderPlacedEvent
│
└── web/
    └── OrderController.java            # REST layer (talks to facades only)
```

## How Communication Works

### Synchronous (Facade calls)
```java
// Controller calls Order module's public facade
OrderDto order = orderFacade.placeOrder("customer-1", "PROD-1", 3);

// Controller calls Inventory module's public facade
int stock = inventoryFacade.getStock("PROD-1");
```

### Asynchronous (Event-driven)
```java
// Order module PUBLISHES:
eventBus.publish(new OrderPlacedEvent(...));

// Inventory module SUBSCRIBES and reacts:
eventBus.subscribe(OrderPlacedEvent.class, this::onOrderPlaced);

// Notification module also SUBSCRIBES:
eventBus.subscribe(OrderPlacedEvent.class, this::onOrderPlaced);
```

The order module doesn't know about inventory or notifications. It just publishes what happened.

## When to Evolve to Microservices

A Modular Monolith makes microservice extraction trivial:
- Each module already has a clean facade → becomes an API
- Each module already owns its data → becomes its own database
- Events already decouple modules → becomes Kafka/SQS messages

Extract when you have a genuine reason: independent scaling, different deployment cadences, or team autonomy needs.

## Running this module

```bash
./gradlew :architecture:modular-monolith:bootRun
```

### REST API

```bash
# Place an order (triggers inventory reservation + notification via events)
curl -X POST http://localhost:8095/api/modular/orders \
  -H "Content-Type: application/json" \
  -d '{"customerId": "cust-1", "productId": "PROD-1", "quantity": 2}'

# Get an order by ID
curl http://localhost:8095/api/modular/orders/{orderId}

# Check stock
curl http://localhost:8095/api/modular/inventory/PROD-1
```
