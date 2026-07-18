package com.fulfillflow.fulfillment;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
class OrderEventConsumerTests {
    @Autowired private OrderEventConsumer consumer;
    @Autowired private JdbcTemplate jdbc;

    @AfterEach
    void cleanDatabase() {
        jdbc.update("DELETE FROM fulfillment_tasks");
        jdbc.update("DELETE FROM order_items");
        jdbc.update("DELETE FROM orders");
    }

    @Test
    void createsOneTaskWhenEventIsDeliveredTwice() {
        var orderId = insertOrder();
        var payload = "{\"orderId\":\"%s\"}".formatted(orderId);

        consumer.handle(payload, "order.created");
        consumer.handle(payload, "order.created");

        assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM fulfillment_tasks WHERE order_id = ?", Integer.class, orderId))
                .isEqualTo(1);
    }

    @Test
    void completesTaskFromOrderEvent() {
        var orderId = insertOrder();
        var payload = "{\"orderId\":\"%s\"}".formatted(orderId);

        consumer.handle(payload, "order.created");
        consumer.handle(payload, "order.completed");

        assertThat(jdbc.queryForObject(
                "SELECT status FROM fulfillment_tasks WHERE order_id = ?", String.class, orderId))
                .isEqualTo("COMPLETED");
    }

    private UUID insertOrder() {
        var id = UUID.randomUUID();
        var now = Instant.now();
        jdbc.update("""
                INSERT INTO orders (id, order_number, customer_name, status, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?)
                """, id, "TEST-" + id.toString().substring(0, 8), "Test Customer", "READY_TO_PICK", now, now);
        return id;
    }
}
