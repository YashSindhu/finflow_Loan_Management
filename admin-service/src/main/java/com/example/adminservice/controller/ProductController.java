package com.example.adminservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/products")
public class ProductController {

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getProducts() {
        List<Map<String, Object>> products = List.of(
            Map.of(
                "id", 1,
                "name", "Personal Loan",
                "description", "Quick personal loans with minimal documentation",
                "minAmount", 50000,
                "maxAmount", 1500000,
                "minTenureMonths", 12,
                "maxTenureMonths", 60,
                "interestRatePercent", 10.5
            ),
            Map.of(
                "id", 2,
                "name", "Home Loan",
                "description", "Affordable home loans for your dream house",
                "minAmount", 500000,
                "maxAmount", 50000000,
                "minTenureMonths", 60,
                "maxTenureMonths", 300,
                "interestRatePercent", 8.5
            ),
            Map.of(
                "id", 3,
                "name", "Business Loan",
                "description", "Grow your business with flexible financing",
                "minAmount", 100000,
                "maxAmount", 10000000,
                "minTenureMonths", 12,
                "maxTenureMonths", 84,
                "interestRatePercent", 12.0
            )
        );
        return ResponseEntity.ok(products);
    }
}
