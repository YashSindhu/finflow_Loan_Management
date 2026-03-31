package com.example.adminservice.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ProductControllerTest {

    private final ProductController controller = new ProductController();

    @Test
    void getProducts_returnsThreeProducts() {
        ResponseEntity<List<Map<String, Object>>> response = controller.getProducts();
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().size());
    }

    @Test
    void getProducts_containsPersonalLoan() {
        ResponseEntity<List<Map<String, Object>>> response = controller.getProducts();
        assertNotNull(response.getBody());
        assertTrue(response.getBody().stream()
                .anyMatch(p -> "Personal Loan".equals(p.get("name"))));
    }

    @Test
    void getProducts_containsHomeLoan() {
        ResponseEntity<List<Map<String, Object>>> response = controller.getProducts();
        assertNotNull(response.getBody());
        assertTrue(response.getBody().stream()
                .anyMatch(p -> "Home Loan".equals(p.get("name"))));
    }

    @Test
    void getProducts_containsBusinessLoan() {
        ResponseEntity<List<Map<String, Object>>> response = controller.getProducts();
        assertNotNull(response.getBody());
        assertTrue(response.getBody().stream()
                .anyMatch(p -> "Business Loan".equals(p.get("name"))));
    }
}
