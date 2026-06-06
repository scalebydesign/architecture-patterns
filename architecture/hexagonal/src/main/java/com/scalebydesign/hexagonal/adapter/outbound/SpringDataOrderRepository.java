package com.scalebydesign.hexagonal.adapter.outbound;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Spring Data JPA Repository — pure infrastructure.
 * This is NOT exposed to the domain. Only the adapter uses it internally.
 */
public interface SpringDataOrderRepository extends JpaRepository<OrderJpaEntity, UUID> {
}
