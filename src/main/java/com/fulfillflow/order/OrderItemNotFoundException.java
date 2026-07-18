package com.fulfillflow.order;

import java.util.UUID;

public class OrderItemNotFoundException extends RuntimeException {
    public OrderItemNotFoundException(UUID id) {
        super("Order item " + id + " was not found");
    }
}
