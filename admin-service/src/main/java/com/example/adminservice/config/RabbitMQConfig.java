package com.example.adminservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE        = "finflow.exchange";
    public static final String QUEUE           = "application.submitted.queue";
    public static final String ROUTING_KEY     = "application.submitted";

    public static final String DLQ_EXCHANGE    = "finflow.dlq.exchange";
    public static final String DLQ_QUEUE       = "application.submitted.dlq";
    public static final String DLQ_ROUTING_KEY = "application.submitted.dead";

    @Bean
    public TopicExchange finflowExchange() {
        return new TopicExchange(EXCHANGE);
    }

    // Dead Letter Exchange
    @Bean
    public DirectExchange dlqExchange() {
        return new DirectExchange(DLQ_EXCHANGE);
    }

    // Dead Letter Queue
    @Bean
    public Queue dlqQueue() {
        return new Queue(DLQ_QUEUE, true);
    }

    // Bind DLQ to DLQ Exchange
    @Bean
    public Binding dlqBinding() {
        return BindingBuilder.bind(dlqQueue()).to(dlqExchange()).with(DLQ_ROUTING_KEY);
    }

    // Main queue with DLQ configured
    @Bean
    public Queue submittedQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", DLQ_EXCHANGE);
        args.put("x-dead-letter-routing-key", DLQ_ROUTING_KEY);
        return new Queue(QUEUE, true, false, false, args);
    }

    @Bean
    public Binding submittedBinding(Queue submittedQueue, TopicExchange finflowExchange) {
        return BindingBuilder.bind(submittedQueue).to(finflowExchange).with(ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
