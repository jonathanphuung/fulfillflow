package com.fulfillflow.order;

import com.fulfillflow.inventory.CreateReservationRequest;
import com.fulfillflow.inventory.ReservationService;
import com.fulfillflow.messaging.OutboxService;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

@Service
class OrderService {
    private final OrderRepository orders;
    private final ReservationService reservations;
    private final OutboxService outbox;
    private final Counter ordersCreated;
    private final Counter ordersCompleted;

    OrderService(OrderRepository orders, ReservationService reservations, OutboxService outbox,
            MeterRegistry meterRegistry) {
        this.orders = orders;
        this.reservations = reservations;
        this.outbox = outbox;
        this.ordersCreated = meterRegistry.counter("fulfillflow.orders.created");
        this.ordersCompleted = meterRegistry.counter("fulfillflow.orders.completed");
    }

    @Transactional
    OrderResponse create(CreateOrderRequest request) {
        var order = new FulfillmentOrder(request.customerName().trim());
        for (int index = 0; index < request.items().size(); index++) {
            var item = request.items().get(index);
            var reservation = reservations.create(
                    item.productId(),
                    new CreateReservationRequest(item.quantity()),
                    "order-" + order.getId() + "-item-" + index);
            order.addItem(item.productId(), reservation.id(), item.quantity());
        }
        order.markReady();
        var saved = orders.save(order);
        record(saved, "order.created");
        ordersCreated.increment();
        return OrderResponse.from(saved);
    }

    @Transactional(readOnly = true)
    OrderResponse get(UUID id) {
        return orders.findById(id).map(OrderResponse::from)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    @Transactional
    OrderResponse startPicking(UUID id) {
        var order = findForUpdate(id);
        order.startPicking();
        record(order, "order.picking.started");
        return OrderResponse.from(order);
    }

    @Transactional
    OrderResponse pickItem(UUID orderId, UUID itemId) {
        var order = findForUpdate(orderId);
        var item = order.findItem(itemId);
        reservations.complete(item.markPicked());
        record(order, "order.item.picked");
        return OrderResponse.from(order);
    }

    @Transactional
    OrderResponse markUnavailable(UUID orderId, UUID itemId) {
        var order = findForUpdate(orderId);
        var item = order.findItem(itemId);
        reservations.release(item.markUnavailable());
        record(order, "order.item.unavailable");
        return OrderResponse.from(order);
    }

    @Transactional
    OrderResponse complete(UUID id) {
        var order = findForUpdate(id);
        order.complete();
        record(order, "order.completed");
        ordersCompleted.increment();
        return OrderResponse.from(order);
    }

    @Transactional
    OrderResponse cancel(UUID id) {
        var order = findForUpdate(id);
        order.cancel().forEach(reservations::release);
        record(order, "order.cancelled");
        return OrderResponse.from(order);
    }

    private FulfillmentOrder findForUpdate(UUID id) {
        return orders.findForUpdateById(id).orElseThrow(() -> new OrderNotFoundException(id));
    }

    private void record(FulfillmentOrder order, String eventType) {
        var payload = "{\"orderId\":\"%s\",\"orderNumber\":\"%s\",\"status\":\"%s\"}"
                .formatted(order.getId(), order.getOrderNumber(), order.getStatus().name());
        outbox.record(order.getId(), eventType, payload);
    }
}
