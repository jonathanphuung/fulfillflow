package com.fulfillflow.fulfillment;

import java.util.UUID;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class OrderEventConsumer {
    private final FulfillmentTaskService tasks;
    private final ObjectMapper objectMapper;

    OrderEventConsumer(FulfillmentTaskService tasks, ObjectMapper objectMapper) {
        this.tasks = tasks;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = "fulfillflow.order-processing")
    public void handle(String payload, @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String eventType) {
        var event = objectMapper.readTree(payload);
        var orderId = UUID.fromString(event.required("orderId").asText());
        switch (eventType) {
            case "order.created" -> tasks.createForOrder(orderId);
            case "order.completed" -> tasks.completeForOrder(orderId);
            case "order.cancelled" -> tasks.cancelForOrder(orderId);
            default -> { }
        }
    }
}
