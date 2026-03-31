package com.example.adminservice.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DtoTest {

    @Test
    void decisionRequest_gettersSetters() {
        DecisionRequest req = new DecisionRequest();
        req.setDecisionType("APPROVED");
        req.setRemarks("All good");
        req.setApprovedAmount(500000.0);
        req.setInterestRate(10.5);
        req.setTenureMonths(36);

        assertEquals("APPROVED", req.getDecisionType());
        assertEquals("All good", req.getRemarks());
        assertEquals(500000.0, req.getApprovedAmount());
        assertEquals(10.5, req.getInterestRate());
        assertEquals(36, req.getTenureMonths());
    }

    @Test
    void userUpdateRequest_gettersSetters() {
        UserUpdateRequest req = new UserUpdateRequest();
        req.setRole("ROLE_ADMIN");
        req.setEnabled(true);

        assertEquals("ROLE_ADMIN", req.getRole());
        assertTrue(req.getEnabled());
    }
}
