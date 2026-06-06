package com.scalebydesign.clean.usecase.port;

import com.scalebydesign.clean.entity.User;

import java.util.Optional;
import java.util.UUID;

/**
 * USE CASE LAYER - Gateway Interface
 * 
 * In Clean Architecture, the Use Case layer defines "gateway" interfaces
 * that the Interface Adapter layer must implement.
 * 
 * This is equivalent to Hexagonal's "outbound port."
 */
public interface UserGateway {

    User save(User user);

    Optional<User> findById(UUID id);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
