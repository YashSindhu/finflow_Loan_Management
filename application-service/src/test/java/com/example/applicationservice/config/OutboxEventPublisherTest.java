package com.example.applicationservice.config;

import com.example.applicationservice.entity.OutboxEvent;
import com.example.applicationservice.repository.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxEventPublisherTest {

    @Mock
    private OutboxEventRepository outboxRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private OutboxEventPublisher outboxEventPublisher;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        // inject real ObjectMapper
        org.springframework.test.util.ReflectionTestUtils.setField(outboxEventPublisher, "objectMapper", objectMapper);
    }

    @Test
    void publishPendingEvents_doesNothingWhenNoPendingEvents() {
        when(outboxRepository.findByPublishedFalse()).thenReturn(List.of());

        outboxEventPublisher.publishPendingEvents();

        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    void publishPendingEvents_publishesEventAndMarksAsPublished() {
        OutboxEvent event = new OutboxEvent("APPLICATION_SUBMITTED",
                "{\"applicationId\":1,\"applicantEmail\":\"user@test.com\",\"event\":\"APPLICATION_SUBMITTED\"}");

        when(outboxRepository.findByPublishedFalse()).thenReturn(List.of(event));

        outboxEventPublisher.publishPendingEvents();

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE),
                eq(RabbitMQConfig.ROUTING_KEY),
                any(Object.class)
        );

        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxRepository).save(captor.capture());
        assertTrue(captor.getValue().isPublished());
        assertNotNull(captor.getValue().getPublishedAt());
    }

    @Test
    void publishPendingEvents_continuesOnFailureForOneEvent() {
        OutboxEvent badEvent = new OutboxEvent("APPLICATION_SUBMITTED", "invalid-json");
        OutboxEvent goodEvent = new OutboxEvent("APPLICATION_SUBMITTED",
                "{\"applicationId\":2,\"applicantEmail\":\"user@test.com\",\"event\":\"APPLICATION_SUBMITTED\"}");

        when(outboxRepository.findByPublishedFalse()).thenReturn(List.of(badEvent, goodEvent));

        outboxEventPublisher.publishPendingEvents();

        // only the good event should be saved as published
        verify(outboxRepository, times(1)).save(any());
    }
}
