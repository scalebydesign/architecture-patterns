# Architecture Patterns

A collection of software architecture and domain modeling patterns implemented in **Java 21 + Spring Boot 4** using an ecommerce domain. Each pattern is a standalone Gradle module that can be run independently.

Built for the [Scale By Design](https://youtube.com/@ScaleByDesign) YouTube channel.

## Patterns Covered

### Architecture Patterns — How to structure layers and dependencies

| Pattern | Module | Port | Domain |
|---------|--------|------|--------|
| Hexagonal (Ports & Adapters) | `architecture/hexagonal` | 8081 | Order Management |
| Onion Architecture | `architecture/onion` | 8082 | Product Catalog |
| Hexagonal + Onion Combined | `architecture/hexagonal-onion` | 8083 | Shopping Cart |
| Clean Architecture | `architecture/clean` | 8084 | User Registration |
| Traditional Layered | `architecture/layered` | 8085 | Inventory Management |

### Domain Modeling Patterns — What goes inside the domain layer

| Pattern | Module | Port | Same Domain |
|---------|--------|------|-------------|
| Rich Domain Model | `domain-modeling/rich-domain` | 8091 | Order |
| Anemic Domain Model | `domain-modeling/anemic-domain` | 8092 | Order |
| CQRS | `domain-modeling/cqrs` | 8093 | Order |
| Event Sourcing | `domain-modeling/event-sourcing` | 8094 | Order |

> Domain modeling modules all use the **Order** domain so you can compare the same business logic expressed in different styles.

## Quick Start

```bash
# Build all modules
./gradlew build

# Run any module
./gradlew :architecture:hexagonal:bootRun
./gradlew :architecture:onion:bootRun
./gradlew :architecture:clean:bootRun
./gradlew :domain-modeling:rich-domain:bootRun
./gradlew :domain-modeling:cqrs:bootRun
./gradlew :domain-modeling:event-sourcing:bootRun
```

Each module uses an in-memory H2 database — no external setup needed.


## Project Structure

```
architecture-patterns/
├── README.md
├── build.gradle                          # Root build — shared config
├── settings.gradle                       # All module includes
│
├── architecture/                         # CATEGORY: Structural Patterns
│   ├── hexagonal/                       # Ports & Adapters
│   ├── onion/                           # Concentric layers
│   ├── hexagonal-onion/                 # Combined approach
│   ├── clean/                           # Uncle Bob's Clean Architecture
│   └── layered/                         # Traditional Controller→Service→Repo
│
└── domain-modeling/                      # CATEGORY: Domain Model Styles
    ├── rich-domain/                     # Entity has behavior + data
    ├── anemic-domain/                   # Entity is data bag, service has logic
    ├── cqrs/                            # Separate read/write models
    └── event-sourcing/                  # State rebuilt from events
```

## How Architecture and Domain Modeling relate

```
┌─────────────────────────────────────────────────────┐
│  Architecture Pattern (Hexagonal, Onion, Clean)     │
│  → Answers: HOW to organize layers                  │
│  → Answers: WHERE dependencies point                │
│                                                     │
│  ┌───────────────────────────────────────────────┐  │
│  │  Domain Modeling Style (Rich, Anemic, ES)     │  │
│  │  → Answers: WHAT goes inside the domain       │  │
│  │  → Answers: WHERE business logic lives        │  │
│  └───────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────┘
```

They operate at different levels and can be combined:
- Hexagonal + Rich Domain = ideal for complex business logic
- Layered + Anemic = fine for simple CRUD
- Hexagonal + CQRS + Event Sourcing = complex distributed systems

## Tech Stack

- Java 21
- Spring Boot 4.0.6
- Spring Data JPA
- H2 Database (in-memory)
- Gradle (multi-module)
- Logback (structured logging)

## License

MIT
