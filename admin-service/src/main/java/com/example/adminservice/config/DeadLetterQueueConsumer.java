package com.example.adminservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class DeadLetterQueueConsumer {

    private static final Logger log = LoggerFactory.getLogger(DeadLetterQueueConsumer.class);

    @RabbitListener(queues = RabbitMQConfig.DLQ_QUEUE)
    public void handleDeadLetter(Message message) {
        log.error("[DLQ] Dead letter received - message could not be processed");
        log.error("[DLQ] Message body: {}", new String(message.getBody()));
        log.error("[DLQ] Message properties: {}", message.getMessageProperties());
        // In production: save to DB, send alert, trigger retry logic
    }
}
