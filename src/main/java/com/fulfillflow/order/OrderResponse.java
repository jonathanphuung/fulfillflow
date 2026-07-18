package com.fulfillflow.order;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        String orderNumber,
        String customerName,
        String status,
        Instant createdAt,
        List<Item> items) {

    public record Item(UUID id, UUID productId, UUID reservationId, int quantity, String status) {
        static Item from(OrderItem item) {
            return new Item(item.getId(), item.getProductId(), item.getReservationId(),
                    item.getQuantity(), item.getStatus().name());
        }
    }

    static OrderResponse from(FulfillmentOrder order) {
        return new OrderResponse(order.getId(), order.getOrderNumber(), order.getCustomerName(),
                order.getStatus().name(), order.getCreatedAt(),
                order.getItems().stream().map(Item::from).toList());
    }
}
