package com.example.authservice.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleRuntime_returnsBadRequest() {
        RuntimeException ex = new RuntimeException("Something went wrong");
        ResponseEntity<Map<String, String>> response = handler.handleRuntime(ex);
        assertEquals(400, response.getStatusCode().value());
        assertEquals("Something went wrong", response.getBody().get("message"));
    }

    @Test
    void handleException_returnsInternalServerError() {
        Exception ex = new Exception("Unexpected error");
        ResponseEntity<Map<String, String>> response = handler.handleException(ex);
        assertEquals(500, response.getStatusCode().value());
        assertEquals("Unexpected error", response.getBody().get("message"));
    }

    @Test
    void handleValidation_returnsFieldErrors() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("obj", "email", "Email is required");
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<Map<String, String>> response = handler.handleValidation(ex);
        assertEquals(400, response.getStatusCode().value());
        assertEquals("Email is required", response.getBody().get("email"));
    }
}
