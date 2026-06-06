# State Management Patterns

## What Problem Are We Solving?

Many domain entities go through a lifecycle of states with rules about which transitions are valid. For example, an Order moves through `DRAFT → SUBMITTED → APPROVED → COMPLETED`, and you can't skip steps or go backwards.

The question is: **how do you encode and enforce these rules in code?**

This folder demonstrates 4 different approaches to the same state machine, using the same Order domain for direct comparison.

## The State Machine We're Implementing

```
        ┌─────────── cancel() ───────────────────────────────────┐
        │                                                         │
        ▼                                                         │
  ┌───────────┐   submit()   ┌───────────┐   approve()   ┌──────┴────┐   complete()   ┌───────────┐
  │   DRAFT   │ ──────────→  │ SUBMITTED │ ──────────→    │  APPROVED │ ──────────→    │ COMPLETED │
  └───────────┘              └───────────┘                └───────────┘               └───────────┘
        │                          │                           │
        │        cancel()          │        cancel()           │
        └──────────┬───────────────┴───────────────────────────┘
                   ▼
             ┌───────────┐
             │ CANCELLED │  (terminal — no transitions out)
             └───────────┘
```

## The 4 Approaches


### 1. Guard Methods (`guard-methods/`)

Each transition method has an `if` check that validates the current state.

```java
public void submit() {
    assertState(OrderStatus.DRAFT, "Only draft orders can be submitted");
    this.status = OrderStatus.SUBMITTED;
}
```

| Aspect | Rating |
|--------|--------|
| Simplicity | ⭐⭐⭐⭐⭐ |
| Readability | ⭐⭐⭐⭐ |
| Completeness visibility | ⭐⭐ (scattered across methods) |
| Scalability (10+ states) | ⭐⭐ |
| Framework dependency | None |
| Best for | 3-6 states, simple transitions, small teams |

---

### 2. Transition Table (`transition-table/`)

All valid transitions defined in a centralized `Map<State, Map<Event, TargetState>>`.

```java
private static final Map<OrderStatus, Map<OrderEvent, OrderStatus>> TRANSITIONS = Map.of(
    OrderStatus.DRAFT, Map.of(
        OrderEvent.SUBMIT, OrderStatus.SUBMITTED,
        OrderEvent.CANCEL, OrderStatus.CANCELLED
    ),
    // ...
);
```

| Aspect | Rating |
|--------|--------|
| Simplicity | ⭐⭐⭐⭐ |
| Readability | ⭐⭐⭐⭐⭐ (all transitions in one place) |
| Completeness visibility | ⭐⭐⭐⭐⭐ |
| Scalability (10+ states) | ⭐⭐⭐⭐ |
| Framework dependency | None |
| Best for | Medium complexity, need to visualize/document all transitions |

Bonus: You can expose `getAvailableEvents()` to the UI.

---

### 3. GoF State Pattern (`state-pattern/`)

Each state is a separate class implementing an `OrderState` interface. The entity delegates all behavior to the current state object.

```java
public class DraftState implements OrderState {
    @Override
    public OrderState submit(Order context) {
        return new SubmittedState();
    }
    @Override
    public OrderState approve(Order context) {
        return illegalTransition("approve");
    }
}
```

| Aspect | Rating |
|--------|--------|
| Simplicity | ⭐⭐ |
| Readability | ⭐⭐⭐ (each state class is clear, but many files) |
| Completeness visibility | ⭐⭐ (scattered across classes) |
| Scalability (10+ states) | ⭐⭐⭐⭐⭐ |
| Framework dependency | None |
| Best for | States with very different behavior (calculations, entry/exit actions) |

Key advantage: Each state can have completely different behavior, not just different "allowed transitions."

---

### 4. Spring State Machine (`spring-state-machine/`)

State machine defined declaratively via Spring State Machine framework configuration.

```java
transitions
    .withExternal()
    .source(OrderStatus.DRAFT).target(OrderStatus.SUBMITTED).event(OrderEvent.SUBMIT)
    .and()
    // ...
```

| Aspect | Rating |
|--------|--------|
| Simplicity | ⭐⭐ (framework learning curve) |
| Readability | ⭐⭐⭐⭐ (declarative config is clean) |
| Completeness visibility | ⭐⭐⭐⭐⭐ (all in config class) |
| Scalability (10+ states) | ⭐⭐⭐⭐⭐ |
| Framework dependency | Heavy (Spring State Machine) |
| Best for | Enterprise apps needing persistence, guards, actions, regions |

Additional features: guards (conditional transitions), actions (side effects), persistence, hierarchical states, parallel regions.


## Comparison Matrix

| Criteria | Guard Methods | Transition Table | State Pattern | Spring SM |
|----------|:---:|:---:|:---:|:---:|
| Lines of code (this example) | ~50 | ~70 | ~120 (6 files) | ~80 + framework |
| Can see all transitions at once | ❌ | ✅ | ❌ | ✅ |
| Per-state behavior (entry/exit) | ❌ | ❌ | ✅ | ✅ |
| UI can query "what's allowed?" | ❌ | ✅ | ⚠️ (via interface) | ✅ |
| Persistence built-in | ❌ | ❌ | ❌ | ✅ |
| Guards (conditional transitions) | Manual if | Manual if | In state class | Built-in |
| Zero dependencies | ✅ | ✅ | ✅ | ❌ |
| Testability | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐ |

## When to Use What

```
Number of states:   3-5        6-10        10-20        20+
                     │           │            │           │
                     ▼           ▼            ▼           ▼
Recommended:     Guard      Transition    State       Spring SM
                Methods      Table       Pattern     (or custom
                                                    framework)
```

| Scenario | Best approach |
|----------|--------------|
| Simple Order (DRAFT → CONFIRMED → PAID) | Guard methods |
| Insurance claim (10 states, need documentation) | Transition table |
| Workflow engine (states have very different UI/logic) | State pattern |
| Enterprise with audit, persistence, parallel states | Spring State Machine |
| Need to load transitions from config file/DB | Transition table |
| Need to expose available actions to frontend | Transition table or Spring SM |
| Need entry/exit actions on state change | State pattern or Spring SM |

## Advanced Concepts (Not Shown)

### Hierarchical States (Sub-states)
```
PROCESSING
├── PAYMENT_PENDING
├── PAYMENT_CONFIRMED
└── SHIPPING
```

Supported by: State Pattern (via composition), Spring SM (built-in regions)

### Parallel States (Orthogonal Regions)
```
ORDER
├── Payment: [PENDING → PAID]          (independent track)
├── Shipping: [PREPARING → SHIPPED]    (independent track)
└── Combined: COMPLETED when both done
```

Supported by: Spring SM only (built-in)

### Persistent State Machines
Store current state in DB, reload on next interaction. Critical for:
- Long-running workflows (days/weeks between transitions)
- Stateless services (microservices, serverless)
- Audit trail requirements

Supported by: Spring SM (built-in JPA persister), or manually with transition table

### Guards (Conditional Transitions)
```java
// Transition only allowed if certain conditions are met
.withExternal()
.source(DRAFT).target(SUBMITTED).event(SUBMIT)
.guard(context -> context.getOrder().getItems().size() > 0)  // Guard!
```

In guard methods: just an `if` check inside the method.
In Spring SM: first-class concept with `Guard<S, E>` interface.

## Running the Examples

```bash
./gradlew :design-patterns:state-management:guard-methods:bootRun          # Port 8101
./gradlew :design-patterns:state-management:transition-table:bootRun       # Port 8102
./gradlew :design-patterns:state-management:state-pattern:bootRun          # Port 8103
./gradlew :design-patterns:state-management:spring-state-machine:bootRun   # Port 8104
```

## Key Takeaway

> All four approaches enforce the SAME rules. The difference is WHERE the rules are defined and HOW they scale.

Start with Guard Methods (simplest). Graduate to Transition Table when you need visibility. Use State Pattern when states have genuinely different behavior. Bring in Spring State Machine when you need enterprise features (persistence, audit, parallel states).
