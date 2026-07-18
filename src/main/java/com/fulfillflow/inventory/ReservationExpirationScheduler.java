package com.fulfillflow.inventory;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "fulfillflow.expiration.enabled", havingValue = "true", matchIfMissing = true)
class ReservationExpirationScheduler {
    private final ReservationExpirationService expiration;

    ReservationExpirationScheduler(ReservationExpirationService expiration) {
        this.expiration = expiration;
    }

    @Scheduled(fixedDelayString = "${fulfillflow.expiration.interval:30000}")
    void expireReservations() {
        expiration.expireBatch();
    }
}
