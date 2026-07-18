package com.fulfillflow.messaging;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_events")
class OutboxEvent {
    @Id private UUID id;
    @Column(name = "aggregate_type", nullable = false, length = 60) private String aggregateType;
    @Column(name = "aggregate_id", nullable = false) private UUID aggregateId;
    @Column(name = "event_type", nullable = false, length = 100) private String eventType;
    @Column(nullable = false, columnDefinition = "TEXT") private String payload;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private OutboxStatus status;
    @Column(nullable = false) private int attempts;
    @Column(name = "available_at", nullable = false) private Instant availableAt;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "published_at") private Instant publishedAt;

    protected OutboxEvent() {
    }

    OutboxEvent(String aggregateType, UUID aggregateId, String eventType, String payload) {
        var now = Instant.now();
        this.id = UUID.randomUUID();
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
        this.status = OutboxStatus.PENDING;
        this.availableAt = now;
        this.createdAt = now;
    }

    UUID getId() { return id; }
    String getEventType() { return eventType; }
    String getPayload() { return payload; }

    void published() {
        status = OutboxStatus.PUBLISHED;
        publishedAt = Instant.now();
        attempts++;
    }

    void retry() {
        attempts++;
        status = attempts >= 5 ? OutboxStatus.FAILED : OutboxStatus.PENDING;
        availableAt = Instant.now().plusSeconds(Math.min(60, attempts * attempts * 2L));
    }
}
