package com.fulfillflow.messaging;

import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class OutboxService {
    private final OutboxRepository events;

    OutboxService(OutboxRepository events) {
        this.events = events;
    }

    public void record(UUID aggregateId, String eventType, String payload) {
        events.save(new OutboxEvent("ORDER", aggregateId, eventType, payload));
    }
}
