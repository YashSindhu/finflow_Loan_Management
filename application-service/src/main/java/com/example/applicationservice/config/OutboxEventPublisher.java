package com.example.applicationservice.config;

import com.example.applicationservice.entity.OutboxEvent;
import com.example.applicationservice.repository.OutboxEventRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
public class OutboxEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxEventPublisher.class);

    @Autowired
    private OutboxEventRepository outboxRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 5000)
    public void publishPendingEvents() {
        List<OutboxEvent> pending = outboxRepository.findByPublishedFalse();
        if (pending.isEmpty()) return;

        log.info("[Outbox] Found {} unpublished event(s), processing...", pending.size());

        for (OutboxEvent event : pending) {
            try {
                Map<String, Object> payload = objectMapper.readValue(
                        event.getPayload(), new TypeReference<>() {});

                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.EXCHANGE,
                        RabbitMQConfig.ROUTING_KEY,
                        payload
                );

                event.setPublished(true);
                event.setPublishedAt(LocalDateTime.now());
                outboxRepository.save(event);

                log.info("[Outbox] Event id: {} published successfully to RabbitMQ", event.getId());
            } catch (Exception e) {
                log.error("[Outbox] Failed to publish event id: {} - {}", event.getId(), e.getMessage());
            }
        }
    }
}
