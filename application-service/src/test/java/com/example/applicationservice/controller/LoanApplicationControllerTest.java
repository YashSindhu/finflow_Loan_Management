package com.example.applicationservice.controller;

import com.example.applicationservice.dto.LoanApplicationRequest;
import com.example.applicationservice.entity.LoanApplication;
import com.example.applicationservice.entity.LoanApplication.ApplicationStatus;
import com.example.applicationservice.service.LoanApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanApplicationControllerTest {

    @Mock
    private LoanApplicationService service;

    private LoanApplicationController controller;

    private LoanApplication app;

    @BeforeEach
    void setUp() {
        controller = new LoanApplicationController();
        ReflectionTestUtils.setField(controller, "service", service);

        app = new LoanApplication();
        app.setId(1L);
        app.setApplicantEmail("user@test.com");
        app.setStatus(ApplicationStatus.DRAFT);
    }

    @Test
    void create_returnsOk() {
        LoanApplicationRequest req = new LoanApplicationRequest();
        when(service.create("user@test.com", req)).thenReturn(app);
        ResponseEntity<LoanApplication> response = controller.create("user@test.com", req);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }

    @Test
    void update_returnsOk() {
        LoanApplicationRequest req = new LoanApplicationRequest();
        when(service.update(1L, "user@test.com", req)).thenReturn(app);
        ResponseEntity<LoanApplication> response = controller.update(1L, "user@test.com", req);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void submit_returnsOk() {
        app.setStatus(ApplicationStatus.SUBMITTED);
        when(service.submit(1L, "user@test.com")).thenReturn(app);
        ResponseEntity<LoanApplication> response = controller.submit(1L, "user@test.com");
        assertEquals(200, response.getStatusCode().value());
        assertEquals(ApplicationStatus.SUBMITTED, response.getBody().getStatus());
    }

    @Test
    void getMyApplications_returnsOk() {
        when(service.getMyApplications("user@test.com")).thenReturn(List.of(app));
        ResponseEntity<List<LoanApplication>> response = controller.getMyApplications("user@test.com");
        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void getStatus_returnsOk() {
        when(service.getStatus(1L, "user@test.com")).thenReturn(app);
        ResponseEntity<LoanApplication> response = controller.getStatus(1L, "user@test.com");
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void getAll_returnsOkForAdmin() {
        when(service.getAll()).thenReturn(List.of(app));
        ResponseEntity<List<LoanApplication>> response = controller.getAll("ROLE_ADMIN");
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void getAll_returns403ForNonAdmin() {
        ResponseEntity<List<LoanApplication>> response = controller.getAll("ROLE_USER");
        assertEquals(403, response.getStatusCode().value());
        verify(service, never()).getAll();
    }

    @Test
    void getById_returnsOkForAdmin() {
        when(service.getById(1L)).thenReturn(app);
        ResponseEntity<LoanApplication> response = controller.getById(1L, "ROLE_ADMIN");
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void getById_returns403ForNonAdmin() {
        ResponseEntity<LoanApplication> response = controller.getById(1L, "ROLE_USER");
        assertEquals(403, response.getStatusCode().value());
    }

    @Test
    void updateStatus_returnsOk() {
        app.setStatus(ApplicationStatus.APPROVED);
        when(service.updateStatus(1L, ApplicationStatus.APPROVED)).thenReturn(app);
        ResponseEntity<LoanApplication> response = controller.updateStatus(1L, Map.of("status", "APPROVED"));
        assertEquals(200, response.getStatusCode().value());
    }
}
