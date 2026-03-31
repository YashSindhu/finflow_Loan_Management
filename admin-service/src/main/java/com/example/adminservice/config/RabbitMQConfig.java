package com.example.adminservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE    = "finflow.exchange";
    public static final String QUEUE       = "application.submitted.queue";
    public static final String ROUTING_KEY = "application.submitted";

    @Bean
    public TopicExchange finflowExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue submittedQueue() {
        return new Queue(QUEUE, true);
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
