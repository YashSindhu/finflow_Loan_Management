package com.example.adminservice.config;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ApplicationEventConsumerTest {

    private final ApplicationEventConsumer consumer = new ApplicationEventConsumer();

    @Test
    void handleApplicationSubmitted_doesNotThrow() {
        Map<String, Object> event = Map.of(
                "applicationId", 1,
                "applicantEmail", "user@test.com",
                "event", "APPLICATION_SUBMITTED"
        );
        assertDoesNotThrow(() -> consumer.handleApplicationSubmitted(event));
    }

    @Test
    void handleApplicationSubmitted_handlesEmptyEvent() {
        assertDoesNotThrow(() -> consumer.handleApplicationSubmitted(Map.of()));
    }
}
