package com.example.documentservice.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VerifyRequestTest {

    @Test
    void verifyRequest_gettersSetters() {
        VerifyRequest req = new VerifyRequest();
        req.setStatus("VERIFIED");
        req.setRemarks("Document looks good");

        assertEquals("VERIFIED", req.getStatus());
        assertEquals("Document looks good", req.getRemarks());
    }
}
