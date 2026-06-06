package com.scalebydesign.clean.interface_adapter.gateway;

import com.scalebydesign.clean.entity.*;
import com.scalebydesign.clean.usecase.port.UserGateway;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * INTERFACE ADAPTER — Gateway Implementation
 * 
 * Implements the UserGateway defined in the use case layer.
 * Translates between domain entities and JPA entities.
 */
@Repository
public class UserJpaGateway implements UserGateway {

    private final SpringDataUserRepository springDataRepo;

    public UserJpaGateway(SpringDataUserRepository springDataRepo) {
        this.springDataRepo = springDataRepo;
    }

    @Override
    public User save(User user) {
        UserJpaEntity entity = toJpa(user);
        UserJpaEntity saved = springDataRepo.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return springDataRepo.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return springDataRepo.findByUsername(username).map(this::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return springDataRepo.findByEmail(email).map(this::toDomain);
    }

    @Override
    public boolean existsByUsername(String username) {
        return springDataRepo.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return springDataRepo.existsByEmail(email);
    }

    private UserJpaEntity toJpa(User user) {
        return new UserJpaEntity(
                user.getId(),
                user.getUsername(),
                user.getEmail().getValue(),
                user.getPassword().getHashedValue(),
                user.getRole().name(),
                user.isActive(),
                user.getCreatedAt()
        );
    }

    private User toDomain(UserJpaEntity entity) {
        User user = new User(entity.getUsername(), entity.getEmail(), "Dummy1Pass");
        user.setId(entity.getId());
        user.setEmail(new Email(entity.getEmail()));
        user.setPassword(Password.fromHash(entity.getPasswordHash()));
        user.setRole(UserRole.valueOf(entity.getRole()));
        user.setActive(entity.isActive());
        user.setCreatedAt(entity.getCreatedAt());
        return user;
    }
}
