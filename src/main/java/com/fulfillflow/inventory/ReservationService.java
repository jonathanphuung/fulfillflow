package com.fulfillflow.inventory;

import com.fulfillflow.catalog.InventoryAllocator;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReservationService {

    private static final Duration RESERVATION_TTL = Duration.ofMinutes(15);

    private final ReservationRepository reservations;
    private final InventoryAllocator inventory;

    ReservationService(ReservationRepository reservations, InventoryAllocator inventory) {
        this.reservations = reservations;
        this.inventory = inventory;
    }

    @Transactional
    public ReservationResponse create(UUID productId, CreateReservationRequest request, String idempotencyKey) {
        var existing = reservations.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            return ReservationResponse.from(existing.get());
        }

        inventory.reserve(productId, request.quantity());

        var reservation = new InventoryReservation(
                productId,
                request.quantity(),
                idempotencyKey,
                Instant.now().plus(RESERVATION_TTL));
        return ReservationResponse.from(reservations.save(reservation));
    }

    @Transactional
    public ReservationResponse release(UUID reservationId) {
        var reservation = reservations.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));
        if (reservation.release()) {
            inventory.release(reservation.getProductId(), reservation.getQuantity());
        }
        return ReservationResponse.from(reservation);
    }
}
