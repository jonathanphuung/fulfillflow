package com.fulfillflow.inventory;

import java.util.Optional;
import java.util.UUID;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Pageable;

interface ReservationRepository extends JpaRepository<InventoryReservation, UUID> {
    Optional<InventoryReservation> findByIdempotencyKey(String idempotencyKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<InventoryReservation> findForUpdateById(UUID id);

    List<InventoryReservation> findByStatusAndExpiresAtLessThanEqualOrderByExpiresAt(
            ReservationStatus status, Instant expiresAt, Pageable pageable);
}
