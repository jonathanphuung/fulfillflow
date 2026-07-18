package com.fulfillflow.inventory;

import java.time.Instant;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
class ReservationExpirationService {
    private final ReservationRepository reservations;
    private final ReservationService reservationService;

    ReservationExpirationService(ReservationRepository reservations, ReservationService reservationService) {
        this.reservations = reservations;
        this.reservationService = reservationService;
    }

    public int expireBatch() {
        var now = Instant.now();
        var expired = reservations.findByStatusAndExpiresAtLessThanEqualOrderByExpiresAt(
                ReservationStatus.ACTIVE, now, PageRequest.of(0, 100));
        var count = 0;
        for (var reservation : expired) {
            if (reservationService.expire(reservation.getId(), now)) {
                count++;
            }
        }
        return count;
    }
}
