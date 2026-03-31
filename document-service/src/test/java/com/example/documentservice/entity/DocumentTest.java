package com.example.documentservice.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DocumentTest {

    @Test
    void document_gettersSetters() {
        Document doc = new Document();
        LocalDateTime now = LocalDateTime.now();

        doc.setId(1L);
        doc.setApplicationId(1L);
        doc.setApplicantEmail("user@test.com");
        doc.setDocumentType("AADHAAR");
        doc.setFileName("aadhaar.pdf");
        doc.setFilePath("/uploads/aadhaar.pdf");
        doc.setStatus(Document.DocumentStatus.PENDING);
        doc.setRemarks("Pending review");
        doc.setUploadedAt(now);
        doc.setVerifiedAt(now);

        assertEquals(1L, doc.getId());
        assertEquals(1L, doc.getApplicationId());
        assertEquals("user@test.com", doc.getApplicantEmail());
        assertEquals("AADHAAR", doc.getDocumentType());
        assertEquals("aadhaar.pdf", doc.getFileName());
        assertEquals("/uploads/aadhaar.pdf", doc.getFilePath());
        assertEquals(Document.DocumentStatus.PENDING, doc.getStatus());
        assertEquals("Pending review", doc.getRemarks());
        assertEquals(now, doc.getUploadedAt());
        assertEquals(now, doc.getVerifiedAt());
    }

    @Test
    void document_defaultStatus_isPending() {
        Document doc = new Document();
        assertEquals(Document.DocumentStatus.PENDING, doc.getStatus());
    }
}
