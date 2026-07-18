package com.fulfillflow.order;

import com.fulfillflow.inventory.CreateReservationRequest;
import com.fulfillflow.inventory.ReservationService;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class OrderService {
    private final OrderRepository orders;
    private final ReservationService reservations;

    OrderService(OrderRepository orders, ReservationService reservations) {
        this.orders = orders;
        this.reservations = reservations;
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
        return OrderResponse.from(orders.save(order));
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
        return OrderResponse.from(order);
    }

    @Transactional
    OrderResponse pickItem(UUID orderId, UUID itemId) {
        var order = findForUpdate(orderId);
        var item = order.findItem(itemId);
        reservations.complete(item.markPicked());
        return OrderResponse.from(order);
    }

    @Transactional
    OrderResponse markUnavailable(UUID orderId, UUID itemId) {
        var order = findForUpdate(orderId);
        var item = order.findItem(itemId);
        reservations.release(item.markUnavailable());
        return OrderResponse.from(order);
    }

    @Transactional
    OrderResponse complete(UUID id) {
        var order = findForUpdate(id);
        order.complete();
        return OrderResponse.from(order);
    }

    @Transactional
    OrderResponse cancel(UUID id) {
        var order = findForUpdate(id);
        order.cancel().forEach(reservations::release);
        return OrderResponse.from(order);
    }

    private FulfillmentOrder findForUpdate(UUID id) {
        return orders.findForUpdateById(id).orElseThrow(() -> new OrderNotFoundException(id));
    }
}
