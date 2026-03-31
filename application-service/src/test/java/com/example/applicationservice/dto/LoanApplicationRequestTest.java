package com.example.applicationservice.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoanApplicationRequestTest {

    @Test
    void loanApplicationRequest_gettersSetters() {
        LoanApplicationRequest req = new LoanApplicationRequest();
        req.setFullName("Yash Sindhu");
        req.setPhone("9876543210");
        req.setAddress("123 MG Road Pune");
        req.setDateOfBirth("1998-05-15");
        req.setEmploymentType("SALARIED");
        req.setEmployerName("Infosys");
        req.setMonthlyIncome(75000.0);
        req.setLoanAmount(500000.0);
        req.setTenureMonths(36);
        req.setLoanPurpose("HOME");

        assertEquals("Yash Sindhu", req.getFullName());
        assertEquals("9876543210", req.getPhone());
        assertEquals("123 MG Road Pune", req.getAddress());
        assertEquals("1998-05-15", req.getDateOfBirth());
        assertEquals("SALARIED", req.getEmploymentType());
        assertEquals("Infosys", req.getEmployerName());
        assertEquals(75000.0, req.getMonthlyIncome());
        assertEquals(500000.0, req.getLoanAmount());
        assertEquals(36, req.getTenureMonths());
        assertEquals("HOME", req.getLoanPurpose());
    }
}
