package com.scalebydesign.clean.usecase;

import com.scalebydesign.clean.entity.User;
import com.scalebydesign.clean.usecase.exception.UserAlreadyExistsException;
import com.scalebydesign.clean.usecase.port.UserGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * USE CASE LAYER (second circle in Clean Architecture)
 * 
 * Contains "Application Business Rules" — orchestrates the flow of data
 * to and from entities and directs those entities to use their business rules.
 * 
 * In Clean Architecture, each use case is its own class (Single Responsibility).
 * 
 * This layer:
 * - Depends on Entity layer only
 * - Defines gateway interfaces (implemented by outer layers)
 * - Contains NO framework code
 * - Is application-specific (unlike entities which are enterprise-wide)
 */
public class RegisterUserUseCase {

    private static final Logger log = LoggerFactory.getLogger(RegisterUserUseCase.class);

    private final UserGateway userGateway;

    public RegisterUserUseCase(UserGateway userGateway) {
        this.userGateway = userGateway;
    }

    public User execute(String username, String email, String password) {
        log.info("Registering new user: username={}", username);

        // Uniqueness check (needs gateway — can't live in entity)
        if (userGateway.existsByUsername(username)) {
            throw new UserAlreadyExistsException("username", username);
        }
        if (userGateway.existsByEmail(email)) {
            throw new UserAlreadyExistsException("email", email);
        }

        // Entity creation (entity validates its own invariants)
        User user = new User(username, email, password);

        User saved = userGateway.save(user);
        log.info("User registered successfully: id={}", saved.getId());
        return saved;
    }
}
