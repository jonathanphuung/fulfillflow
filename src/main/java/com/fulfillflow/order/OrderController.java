package com.fulfillflow.order;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
class OrderController {
    private final OrderService orders;

    OrderController(OrderService orders) {
        this.orders = orders;
    }

    @PostMapping
    ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request) {
        var order = orders.create(request);
        return ResponseEntity.created(URI.create("/api/orders/" + order.id())).body(order);
    }

    @GetMapping("/{id}")
    OrderResponse get(@PathVariable UUID id) {
        return orders.get(id);
    }

    @PostMapping("/{id}/start")
    OrderResponse startPicking(@PathVariable UUID id) {
        return orders.startPicking(id);
    }

    @PostMapping("/{orderId}/items/{itemId}/pick")
    OrderResponse pickItem(@PathVariable UUID orderId, @PathVariable UUID itemId) {
        return orders.pickItem(orderId, itemId);
    }

    @PostMapping("/{orderId}/items/{itemId}/unavailable")
    OrderResponse markUnavailable(@PathVariable UUID orderId, @PathVariable UUID itemId) {
        return orders.markUnavailable(orderId, itemId);
    }

    @PostMapping("/{id}/complete")
    OrderResponse complete(@PathVariable UUID id) {
        return orders.complete(id);
    }

    @DeleteMapping("/{id}")
    OrderResponse cancel(@PathVariable UUID id) {
        return orders.cancel(id);
    }
}
