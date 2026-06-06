# Clean Architecture

## What is it?

Clean Architecture, introduced by Robert C. Martin (Uncle Bob) in 2012, organizes code into concentric circles where dependencies point inward. It's an evolution of Hexagonal and Onion architecture with more explicit terminology.

## The Four Circles

```
┌─────────────────────────────────────────────────────────────────┐
│  FRAMEWORKS & DRIVERS (outermost)                               │
│  Spring, JPA, H2, REST                                         │
│                                                                 │
│   ┌─────────────────────────────────────────────────────────┐   │
│   │  INTERFACE ADAPTERS                                      │   │
│   │  Controllers, Presenters, Gateways                      │   │
│   │                                                         │   │
│   │   ┌─────────────────────────────────────────────────┐   │   │
│   │   │  USE CASES (Application Business Rules)         │   │   │
│   │   │  RegisterUserUseCase, LoginUserUseCase          │   │   │
│   │   │                                                 │   │   │
│   │   │   ┌─────────────────────────────────────────┐   │   │   │
│   │   │   │  ENTITIES (Enterprise Business Rules)   │   │   │   │
│   │   │   │  User, Email, Password                  │   │   │   │
│   │   │   │        *** CENTER ***                   │   │   │   │
│   │   │   └─────────────────────────────────────────┘   │   │   │
│   │   └─────────────────────────────────────────────────┘   │   │
│   └─────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
```


## Key Differences from Hexagonal/Onion

| Aspect | Clean | Hexagonal | Onion |
|--------|-------|-----------|-------|
| Circles | 4 explicit | Not numbered | 4 layers |
| Use Cases | Explicit separate layer | Part of application | Application service |
| Each use case | Its own class (SRP) | Methods in a service | Methods in a service |
| Presenter | Explicit concept | Not prescribed | Not prescribed |
| Gateway | Interface in use case layer | Port in domain | Interface in domain |
| Terminology | Entity, Use Case, Gateway | Port, Adapter | Layer, Ring |

## Key Principles

1. **The Dependency Rule** — Source code dependencies only point INWARD
2. **Entities** — Enterprise business rules (would exist even without software)
3. **Use Cases** — Application-specific business rules (one class per use case)
4. **Interface Adapters** — Convert data between use cases and external agents
5. **Frameworks & Drivers** — Glue code, Spring config, DB drivers

## Project Structure

```
clean/
├── entity/                          # Circle 1: Enterprise Business Rules
│   ├── User.java                   # Entity with self-contained business rules
│   ├── Email.java                  # Value Object (self-validating)
│   ├── Password.java              # Value Object (hashing + strength rules)
│   └── UserRole.java             # Enum
│
├── usecase/                         # Circle 2: Application Business Rules
│   ├── RegisterUserUseCase.java   # One use case = one class (SRP)
│   ├── LoginUserUseCase.java
│   ├── GetUserUseCase.java
│   ├── port/
│   │   └── UserGateway.java       # Interface — defined here, implemented outside
│   └── exception/
│       ├── UserNotFoundException.java
│       ├── UserAlreadyExistsException.java
│       └── InvalidCredentialsException.java
│
├── interface_adapter/               # Circle 3: Interface Adapters
│   ├── controller/
│   │   ├── UserController.java    # HTTP → Use Case
│   │   └── GlobalExceptionHandler.java
│   ├── presenter/
│   │   └── UserResponse.java      # Use Case output → API response
│   └── gateway/
│       ├── UserJpaGateway.java    # Implements UserGateway
│       ├── UserJpaEntity.java     # JPA entity (infra concern)
│       └── SpringDataUserRepository.java
│
└── framework/                       # Circle 4: Frameworks & Drivers
    └── BeanConfig.java             # Spring wiring (composition root)
```

## What makes Clean Architecture unique

### 1. One Use Case = One Class

```java
public class RegisterUserUseCase {
    public User execute(String username, String email, String password) { ... }
}
```

Not `UserService.register()`. Each use case is its own class. This means:
- Each class has one reason to change
- Easy to find what the application does — just list the use case classes
- Easy to test in isolation

### 2. The Presenter concept

The Interface Adapter layer has a **Presenter** that formats output:

```java
public record UserResponse(UUID id, String username, String email, ...) {
    public static UserResponse from(User user) {
        // Strips sensitive data, formats for API consumer
    }
}
```

The use case returns a domain entity. The presenter transforms it for the view.

### 3. Entities are "Enterprise" rules

Uncle Bob distinguishes:
- **Enterprise rules** (Entity layer) — rules that exist even without software
- **Application rules** (Use Case layer) — rules specific to THIS application

Example: "Email must be valid format" is an enterprise rule (lives in Entity/Value Object).
"Username must be unique in our system" is an application rule (lives in Use Case — needs DB check).

## Running this module

```bash
./gradlew :clean:bootRun
```

```bash
# Register
curl -X POST http://localhost:8084/api/clean/users/register \
  -H "Content-Type: application/json" \
  -d '{"username": "john_doe", "email": "john@example.com", "password": "SecurePass1"}'

# Login
curl -X POST http://localhost:8084/api/clean/users/login \
  -H "Content-Type: application/json" \
  -d '{"username": "john_doe", "password": "SecurePass1"}'

# Invalid password → 400
curl -X POST http://localhost:8084/api/clean/users/register \
  -H "Content-Type: application/json" \
  -d '{"username": "jane", "email": "jane@example.com", "password": "weak"}'
```
