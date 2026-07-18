package com.fulfillflow.fulfillment;

import java.time.Instant;
import java.util.UUID;

public record FulfillmentTaskResponse(
        UUID id, UUID orderId, String status, String assignedTo, Instant createdAt) {
    static FulfillmentTaskResponse from(FulfillmentTask task) {
        return new FulfillmentTaskResponse(task.getId(), task.getOrderId(), task.getStatus(),
                task.getAssignedTo(), task.getCreatedAt());
    }
}
