package com.fulfillflow.messaging;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

interface OutboxRepository extends JpaRepository<OutboxEvent, UUID> {
    List<OutboxEvent> findByStatusAndAvailableAtLessThanEqualOrderByCreatedAt(
            OutboxStatus status, Instant availableAt, Pageable pageable);
}
