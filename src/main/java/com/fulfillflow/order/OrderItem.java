package com.fulfillflow.order;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "order_items")
class OrderItem {
    @Id private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private FulfillmentOrder order;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "reservation_id", nullable = false, unique = true)
    private UUID reservationId;

    @Column(nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private OrderItemStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected OrderItem() {
    }

    OrderItem(FulfillmentOrder order, UUID productId, UUID reservationId, int quantity) {
        var now = Instant.now();
        this.id = UUID.randomUUID();
        this.order = order;
        this.productId = productId;
        this.reservationId = reservationId;
        this.quantity = quantity;
        this.status = OrderItemStatus.RESERVED;
        this.createdAt = now;
        this.updatedAt = now;
    }

    UUID getId() { return id; }
    UUID getProductId() { return productId; }
    UUID getReservationId() { return reservationId; }
    int getQuantity() { return quantity; }
    OrderItemStatus getStatus() { return status; }
}
