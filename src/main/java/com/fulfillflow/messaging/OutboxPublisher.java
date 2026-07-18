package com.fulfillflow.messaging;

import java.time.Instant;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(name = "fulfillflow.outbox.enabled", havingValue = "true", matchIfMissing = true)
class OutboxPublisher {
    private final OutboxRepository events;
    private final RabbitTemplate rabbit;

    OutboxPublisher(OutboxRepository events, RabbitTemplate rabbit) {
        this.events = events;
        this.rabbit = rabbit;
    }

    @Scheduled(fixedDelayString = "${fulfillflow.outbox.interval:1000}")
    @Transactional
    void publishBatch() {
        var batch = events.findByStatusAndAvailableAtLessThanEqualOrderByCreatedAt(
                OutboxStatus.PENDING, Instant.now(), PageRequest.of(0, 50));
        for (var event : batch) {
            try {
                rabbit.convertAndSend(RabbitTopology.ORDER_EXCHANGE, event.getEventType(), event.getPayload(), message -> {
                    message.getMessageProperties().setMessageId(event.getId().toString());
                    message.getMessageProperties().setContentType("application/json");
                    return message;
                });
                event.published();
            } catch (RuntimeException exception) {
                event.retry();
            }
        }
    }
}
