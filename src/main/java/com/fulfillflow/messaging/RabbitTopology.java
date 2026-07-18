package com.fulfillflow.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class RabbitTopology {
    static final String ORDER_EXCHANGE = "fulfillflow.orders";
    static final String ORDER_QUEUE = "fulfillflow.order-processing";
    static final String DEAD_LETTER_EXCHANGE = "fulfillflow.orders.dlx";
    static final String DEAD_LETTER_QUEUE = "fulfillflow.order-processing.dlq";

    @Bean TopicExchange orderExchange() { return new TopicExchange(ORDER_EXCHANGE, true, false); }
    @Bean DirectExchange deadLetterExchange() { return new DirectExchange(DEAD_LETTER_EXCHANGE, true, false); }
    @Bean Queue orderQueue() {
        return QueueBuilder.durable(ORDER_QUEUE)
                .deadLetterExchange(DEAD_LETTER_EXCHANGE).deadLetterRoutingKey("dead").build();
    }
    @Bean Queue deadLetterQueue() { return QueueBuilder.durable(DEAD_LETTER_QUEUE).build(); }
    @Bean Binding orderBinding(Queue orderQueue, TopicExchange orderExchange) {
        return BindingBuilder.bind(orderQueue).to(orderExchange).with("order.#");
    }
    @Bean Binding deadLetterBinding(Queue deadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with("dead");
    }
}
