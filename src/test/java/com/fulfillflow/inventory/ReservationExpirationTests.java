package com.fulfillflow.inventory;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
class ReservationExpirationTests {
    @Autowired private ReservationExpirationService expiration;
    @Autowired private JdbcTemplate jdbc;

    @AfterEach
    void cleanDatabase() {
        jdbc.update("DELETE FROM inventory_reservations");
        jdbc.update("DELETE FROM inventory");
        jdbc.update("DELETE FROM products");
    }

    @Test
    void releasesStockForExpiredReservation() {
        var productId = insertProductWithReservedStock(5, 2);
        insertReservation(productId, 2, Instant.now().minusSeconds(30));

        assertThat(expiration.expireBatch()).isEqualTo(1);
        assertThat(jdbc.queryForObject(
                "SELECT quantity_reserved FROM inventory WHERE product_id = ?", Integer.class, productId))
                .isZero();
        assertThat(jdbc.queryForObject(
                "SELECT status FROM inventory_reservations", String.class))
                .isEqualTo("EXPIRED");
    }

    @Test
    void leavesFutureReservationActive() {
        var productId = insertProductWithReservedStock(5, 2);
        insertReservation(productId, 2, Instant.now().plusSeconds(300));

        assertThat(expiration.expireBatch()).isZero();
        assertThat(jdbc.queryForObject(
                "SELECT quantity_reserved FROM inventory WHERE product_id = ?", Integer.class, productId))
                .isEqualTo(2);
    }

    private UUID insertProductWithReservedStock(int onHand, int reserved) {
        var id = UUID.randomUUID();
        var now = Instant.now();
        jdbc.update("""
                INSERT INTO products (id, sku, name, active, created_at, updated_at)
                VALUES (?, ?, ?, TRUE, ?, ?)
                """, id, "EXP-" + id.toString().substring(0, 8), "Expiration Test", now, now);
        jdbc.update("""
                INSERT INTO inventory (product_id, quantity_on_hand, quantity_reserved, version, updated_at)
                VALUES (?, ?, ?, 0, ?)
                """, id, onHand, reserved, now);
        return id;
    }

    private void insertReservation(UUID productId, int quantity, Instant expiresAt) {
        var id = UUID.randomUUID();
        var now = Instant.now();
        jdbc.update("""
                INSERT INTO inventory_reservations
                    (id, product_id, quantity, status, idempotency_key, expires_at, created_at, updated_at)
                VALUES (?, ?, ?, 'ACTIVE', ?, ?, ?, ?)
                """, id, productId, quantity, "expiration-" + id, expiresAt, now, now);
    }
}
