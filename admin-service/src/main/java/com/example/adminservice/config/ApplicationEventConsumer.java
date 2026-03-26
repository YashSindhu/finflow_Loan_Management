package com.example.adminservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ApplicationEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(ApplicationEventConsumer.class);

    @RabbitListener(queues = RabbitMQConfig.QUEUE)
    public void handleApplicationSubmitted(Map<String, Object> event) {
        log.info("[RabbitMQ] Event received: {}", event.get("event"));
        log.info("[RabbitMQ] New application submitted - ID: {}, Applicant: {}",
                event.get("applicationId"),
                event.get("applicantEmail"));
        log.info("[RabbitMQ] Application is ready for admin review");
    }
}
