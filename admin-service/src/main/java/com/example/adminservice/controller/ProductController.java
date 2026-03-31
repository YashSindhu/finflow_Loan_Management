package com.example.adminservice.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/products")
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    private static final String DESCRIPTION = "description";
    private static final String MIN_AMOUNT = "minAmount";
    private static final String MAX_AMOUNT = "maxAmount";
    private static final String MIN_TENURE = "minTenureMonths";
    private static final String MAX_TENURE = "maxTenureMonths";
    private static final String INTEREST_RATE = "interestRatePercent";

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getProducts() {
        log.info("GET /products - fetching all loan products");
        List<Map<String, Object>> products = List.of(
            Map.of(
                "id", 1,
                "name", "Personal Loan",
                DESCRIPTION, "Quick personal loans with minimal documentation",
                MIN_AMOUNT, 50000,
                MAX_AMOUNT, 1500000,
                MIN_TENURE, 12,
                MAX_TENURE, 60,
                INTEREST_RATE, 10.5
            ),
            Map.of(
                "id", 2,
                "name", "Home Loan",
                DESCRIPTION, "Affordable home loans for your dream house",
                MIN_AMOUNT, 500000,
                MAX_AMOUNT, 50000000,
                MIN_TENURE, 60,
                MAX_TENURE, 300,
                INTEREST_RATE, 8.5
            ),
            Map.of(
                "id", 3,
                "name", "Business Loan",
                DESCRIPTION, "Grow your business with flexible financing",
                MIN_AMOUNT, 100000,
                MAX_AMOUNT, 10000000,
                MIN_TENURE, 12,
                MAX_TENURE, 84,
                INTEREST_RATE, 12.0
            )
        );
        return ResponseEntity.ok(products);
    }
}
