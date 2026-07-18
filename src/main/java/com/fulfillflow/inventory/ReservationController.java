package com.fulfillflow.inventory;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventory")
class ReservationController {

    private final ReservationService reservations;

    ReservationController(ReservationService reservations) {
        this.reservations = reservations;
    }

    @PostMapping("/{productId}/reservations")
    ResponseEntity<ReservationResponse> create(
            @PathVariable UUID productId,
            @RequestHeader("Idempotency-Key") @NotBlank @Size(max = 100) String idempotencyKey,
            @Valid @RequestBody CreateReservationRequest request) {
        var reservation = reservations.create(productId, request, idempotencyKey);
        return ResponseEntity.created(URI.create("/api/inventory/reservations/" + reservation.id()))
                .body(reservation);
    }

    @DeleteMapping("/reservations/{reservationId}")
    ReservationResponse release(@PathVariable UUID reservationId) {
        return reservations.release(reservationId);
    }
}
