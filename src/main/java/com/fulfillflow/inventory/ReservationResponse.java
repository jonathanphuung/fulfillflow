package com.fulfillflow.inventory;

import java.time.Instant;
import java.util.UUID;

public record ReservationResponse(
        UUID id,
        UUID productId,
        int quantity,
        String status,
        Instant expiresAt,
        Instant createdAt) {

    static ReservationResponse from(InventoryReservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getProductId(),
                reservation.getQuantity(),
                reservation.getStatus().name(),
                reservation.getExpiresAt(),
                reservation.getCreatedAt());
    }
}
