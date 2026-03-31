package com.example.adminservice.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DecisionTest {

    @Test
    void decision_gettersSetters() {
        Decision decision = new Decision();
        LocalDateTime now = LocalDateTime.now();

        decision.setId(1L);
        decision.setApplicationId(1L);
        decision.setAdminEmail("admin@test.com");
        decision.setDecision(Decision.DecisionType.APPROVED);
        decision.setRemarks("Approved");
        decision.setApprovedAmount(500000.0);
        decision.setInterestRate(10.5);
        decision.setTenureMonths(36);
        decision.setDecidedAt(now);

        assertEquals(1L, decision.getId());
        assertEquals(1L, decision.getApplicationId());
        assertEquals("admin@test.com", decision.getAdminEmail());
        assertEquals(Decision.DecisionType.APPROVED, decision.getDecision());
        assertEquals("Approved", decision.getRemarks());
        assertEquals(500000.0, decision.getApprovedAmount());
        assertEquals(10.5, decision.getInterestRate());
        assertEquals(36, decision.getTenureMonths());
        assertEquals(now, decision.getDecidedAt());
    }
}
