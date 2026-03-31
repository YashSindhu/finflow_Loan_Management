package com.example.adminservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.junit.jupiter.api.Assertions.*;

class ConfigTest {

    private final AppConfig appConfig = new AppConfig();
    private final SwaggerConfig swaggerConfig = new SwaggerConfig();

    @Test
    void restTemplate_returnsNotNull() {
        RestTemplate restTemplate = appConfig.restTemplate();
        assertNotNull(restTemplate);
    }

    @Test
    void corsConfigurationSource_returnsNotNull() {
        CorsConfigurationSource source = appConfig.corsConfigurationSource();
        assertNotNull(source);
    }

    @Test
    void customOpenAPI_returnsNotNull() {
        OpenAPI openAPI = swaggerConfig.customOpenAPI();
        assertNotNull(openAPI);
    }

    @Test
    void customOpenAPI_hasSecurityScheme() {
        OpenAPI openAPI = swaggerConfig.customOpenAPI();
        assertNotNull(openAPI.getComponents());
        assertTrue(openAPI.getComponents().getSecuritySchemes().containsKey("BearerAuth"));
    }

    @Test
    void customOpenAPI_hasServerUrl() {
        OpenAPI openAPI = swaggerConfig.customOpenAPI();
        assertFalse(openAPI.getServers().isEmpty());
        assertEquals("http://localhost:8080/gateway", openAPI.getServers().get(0).getUrl());
    }

    @Test
    void rabbitMQConfig_exchangeAndQueue() {
        assertEquals("finflow.exchange", RabbitMQConfig.EXCHANGE);
        assertEquals("application.submitted.queue", RabbitMQConfig.QUEUE);
        assertEquals("application.submitted", RabbitMQConfig.ROUTING_KEY);
    }
}
