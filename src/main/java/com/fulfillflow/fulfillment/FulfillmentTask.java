package com.fulfillflow.fulfillment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "fulfillment_tasks")
class FulfillmentTask {
    @Id private UUID id;
    @Column(name = "order_id", nullable = false, unique = true) private UUID orderId;
    @Column(nullable = false, length = 24) private String status;
    @Column(name = "assigned_to", length = 160) private String assignedTo;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    protected FulfillmentTask() {
    }

    FulfillmentTask(UUID orderId) {
        var now = Instant.now();
        this.id = UUID.randomUUID();
        this.orderId = orderId;
        this.status = "READY";
        this.createdAt = now;
        this.updatedAt = now;
    }

    UUID getId() { return id; }
    UUID getOrderId() { return orderId; }
    String getStatus() { return status; }
    String getAssignedTo() { return assignedTo; }
    Instant getCreatedAt() { return createdAt; }

    void complete() { status = "COMPLETED"; updatedAt = Instant.now(); }
    void cancel() { status = "CANCELLED"; updatedAt = Instant.now(); }
}
