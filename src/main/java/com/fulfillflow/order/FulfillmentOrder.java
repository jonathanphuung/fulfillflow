package com.fulfillflow.order;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
class FulfillmentOrder {

    @Id
    private UUID id;

    @Column(name = "order_number", nullable = false, unique = true, length = 40)
    private String orderNumber;

    @Column(name = "customer_name", nullable = false, length = 160)
    private String customerName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private OrderStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt asc")
    private List<OrderItem> items = new ArrayList<>();

    protected FulfillmentOrder() {
    }

    FulfillmentOrder(String customerName) {
        var now = Instant.now();
        this.id = UUID.randomUUID();
        this.orderNumber = "FF-" + id.toString().substring(0, 8).toUpperCase();
        this.customerName = customerName;
        this.status = OrderStatus.PENDING;
        this.createdAt = now;
        this.updatedAt = now;
    }

    void addItem(UUID productId, UUID reservationId, int quantity) {
        items.add(new OrderItem(this, productId, reservationId, quantity));
    }

    void markReady() {
        status = OrderStatus.READY_TO_PICK;
        updatedAt = Instant.now();
    }

    UUID getId() { return id; }
    String getOrderNumber() { return orderNumber; }
    String getCustomerName() { return customerName; }
    OrderStatus getStatus() { return status; }
    Instant getCreatedAt() { return createdAt; }
    List<OrderItem> getItems() { return List.copyOf(items); }

    void startPicking() {
        requireStatus(OrderStatus.READY_TO_PICK);
        status = OrderStatus.PICKING;
        updatedAt = Instant.now();
    }

    OrderItem findItem(UUID itemId) {
        requireStatus(OrderStatus.PICKING);
        return items.stream().filter(item -> item.getId().equals(itemId)).findFirst()
                .orElseThrow(() -> new OrderItemNotFoundException(itemId));
    }

    void complete() {
        requireStatus(OrderStatus.PICKING);
        var unfinished = items.stream().anyMatch(item -> item.getStatus() == OrderItemStatus.RESERVED);
        if (unfinished) {
            throw new InvalidOrderStateException("All items must be resolved before completing an order");
        }
        status = OrderStatus.COMPLETED;
        updatedAt = Instant.now();
    }

    List<UUID> cancel() {
        if (status == OrderStatus.COMPLETED || status == OrderStatus.CANCELLED) {
            throw new InvalidOrderStateException("Completed or cancelled orders cannot be cancelled");
        }
        var reservations = items.stream().map(OrderItem::cancel).filter(java.util.Objects::nonNull).toList();
        status = OrderStatus.CANCELLED;
        updatedAt = Instant.now();
        return reservations;
    }

    private void requireStatus(OrderStatus required) {
        if (status != required) {
            throw new InvalidOrderStateException(
                    "Order must be %s but is %s".formatted(required, status));
        }
    }
}
