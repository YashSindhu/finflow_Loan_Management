package com.example.applicationservice.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EntityTest {

    @Test
    void loanApplication_gettersSetters() {
        LoanApplication app = new LoanApplication();
        LocalDateTime now = LocalDateTime.now();

        app.setId(1L);
        app.setApplicantEmail("user@test.com");
        app.setFullName("Yash Sindhu");
        app.setPhone("9876543210");
        app.setAddress("123 MG Road");
        app.setDateOfBirth("1998-05-15");
        app.setEmploymentType("SALARIED");
        app.setEmployerName("Infosys");
        app.setMonthlyIncome(75000.0);
        app.setLoanAmount(500000.0);
        app.setTenureMonths(36);
        app.setLoanPurpose("HOME");
        app.setStatus(LoanApplication.ApplicationStatus.DRAFT);
        app.setCreatedAt(now);
        app.setUpdatedAt(now);

        assertEquals(1L, app.getId());
        assertEquals("user@test.com", app.getApplicantEmail());
        assertEquals("Yash Sindhu", app.getFullName());
        assertEquals("9876543210", app.getPhone());
        assertEquals("123 MG Road", app.getAddress());
        assertEquals("1998-05-15", app.getDateOfBirth());
        assertEquals("SALARIED", app.getEmploymentType());
        assertEquals("Infosys", app.getEmployerName());
        assertEquals(75000.0, app.getMonthlyIncome());
        assertEquals(500000.0, app.getLoanAmount());
        assertEquals(36, app.getTenureMonths());
        assertEquals("HOME", app.getLoanPurpose());
        assertEquals(LoanApplication.ApplicationStatus.DRAFT, app.getStatus());
        assertEquals(now, app.getCreatedAt());
        assertEquals(now, app.getUpdatedAt());
    }

    @Test
    void outboxEvent_gettersSetters() {
        OutboxEvent event = new OutboxEvent("APPLICATION_SUBMITTED", "{\"id\":1}");
        LocalDateTime now = LocalDateTime.now();

        event.setEventType("APPLICATION_SUBMITTED");
        event.setPayload("{\"id\":1}");
        event.setPublished(true);
        event.setPublishedAt(now);

        assertEquals("APPLICATION_SUBMITTED", event.getEventType());
        assertEquals("{\"id\":1}", event.getPayload());
        assertTrue(event.isPublished());
        assertEquals(now, event.getPublishedAt());
        assertNotNull(event.getCreatedAt());
        assertNull(event.getId());
    }

    @Test
    void outboxEvent_defaultConstructor() {
        OutboxEvent event = new OutboxEvent();
        assertFalse(event.isPublished());
        assertNotNull(event.getCreatedAt());
    }
}
