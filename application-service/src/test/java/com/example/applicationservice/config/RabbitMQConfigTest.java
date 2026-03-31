package com.example.applicationservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;

import static org.junit.jupiter.api.Assertions.*;

class RabbitMQConfigTest {

    private final RabbitMQConfig config = new RabbitMQConfig();

    @Test
    void exchange_returnsTopicExchange() {
        TopicExchange exchange = config.exchange();
        assertNotNull(exchange);
        assertEquals(RabbitMQConfig.EXCHANGE, exchange.getName());
    }

    @Test
    void queue_returnsDurableQueue() {
        Queue queue = config.queue();
        assertNotNull(queue);
        assertEquals(RabbitMQConfig.QUEUE, queue.getName());
        assertTrue(queue.isDurable());
    }

    @Test
    void objectMapper_returnsNotNull() {
        ObjectMapper mapper = config.objectMapper();
        assertNotNull(mapper);
    }

    @Test
    void messageConverter_returnsNotNull() {
        Jackson2JsonMessageConverter converter = config.messageConverter();
        assertNotNull(converter);
    }
}
