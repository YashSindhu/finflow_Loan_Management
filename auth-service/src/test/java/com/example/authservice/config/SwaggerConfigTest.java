package com.example.authservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SwaggerConfigTest {

    private final SwaggerConfig swaggerConfig = new SwaggerConfig();

    @Test
    void customOpenAPI_returnsNotNull() {
        OpenAPI openAPI = swaggerConfig.customOpenAPI();
        assertNotNull(openAPI);
    }

    @Test
    void customOpenAPI_hasSecurityScheme() {
        OpenAPI openAPI = swaggerConfig.customOpenAPI();
        assertNotNull(openAPI.getComponents());
        assertNotNull(openAPI.getComponents().getSecuritySchemes());
        assertTrue(openAPI.getComponents().getSecuritySchemes().containsKey("BearerAuth"));
    }

    @Test
    void customOpenAPI_hasServerUrl() {
        OpenAPI openAPI = swaggerConfig.customOpenAPI();
        assertNotNull(openAPI.getServers());
        assertFalse(openAPI.getServers().isEmpty());
        assertEquals("http://localhost:8080/gateway", openAPI.getServers().get(0).getUrl());
    }
}
