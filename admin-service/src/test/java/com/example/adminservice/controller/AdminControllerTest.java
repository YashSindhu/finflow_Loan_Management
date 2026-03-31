package com.example.adminservice.controller;

import com.example.adminservice.dto.DecisionRequest;
import com.example.adminservice.dto.UserUpdateRequest;
import com.example.adminservice.entity.Decision;
import com.example.adminservice.entity.Decision.DecisionType;
import com.example.adminservice.service.AdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private AdminService adminService;

    private AdminController controller;

    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_USER = "ROLE_USER";

    @BeforeEach
    void setUp() {
        controller = new AdminController(adminService);
    }

    // --- getAllApplications ---

    @Test
    void getAllApplications_returnsOkForAdmin() {
        when(adminService.getAllApplications()).thenReturn(List.of(Map.of("id", 1)));
        ResponseEntity<List<Map<String, Object>>> response = controller.getAllApplications(ROLE_ADMIN);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }

    @Test
    void getAllApplications_returns403ForNonAdmin() {
        ResponseEntity<List<Map<String, Object>>> response = controller.getAllApplications(ROLE_USER);
        assertEquals(403, response.getStatusCode().value());
        verify(adminService, never()).getAllApplications();
    }

    // --- getApplication ---

    @Test
    void getApplication_returnsOkForAdmin() {
        when(adminService.getApplicationById(1L)).thenReturn(Map.of("id", 1L));
        ResponseEntity<Map<String, Object>> response = controller.getApplication(1L, ROLE_ADMIN);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void getApplication_returns403ForNonAdmin() {
        ResponseEntity<Map<String, Object>> response = controller.getApplication(1L, ROLE_USER);
        assertEquals(403, response.getStatusCode().value());
    }

    // --- makeDecision ---

    @Test
    void makeDecision_returnsOkForAdmin() {
        DecisionRequest req = new DecisionRequest();
        req.setDecisionType("APPROVED");
        Decision decision = new Decision();
        decision.setDecision(DecisionType.APPROVED);
        when(adminService.makeDecision(1L, "admin@test.com", req)).thenReturn(decision);

        ResponseEntity<Decision> response = controller.makeDecision(1L, "admin@test.com", ROLE_ADMIN, req);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void makeDecision_returns403ForNonAdmin() {
        DecisionRequest req = new DecisionRequest();
        ResponseEntity<Decision> response = controller.makeDecision(1L, "admin@test.com", ROLE_USER, req);
        assertEquals(403, response.getStatusCode().value());
    }

    // --- getDecision ---

    @Test
    void getDecision_returnsOkForAdmin() {
        Decision decision = new Decision();
        when(adminService.getDecisionByApplication(1L)).thenReturn(decision);
        ResponseEntity<Decision> response = controller.getDecision(1L, ROLE_ADMIN);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void getDecision_returns403ForNonAdmin() {
        ResponseEntity<Decision> response = controller.getDecision(1L, ROLE_USER);
        assertEquals(403, response.getStatusCode().value());
    }

    // --- getReports ---

    @Test
    void getReports_returnsOkForAdmin() {
        when(adminService.getReports()).thenReturn(Map.of("totalApplications", 1L));
        ResponseEntity<Map<String, Object>> response = controller.getReports(ROLE_ADMIN);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void getReports_returns403ForNonAdmin() {
        ResponseEntity<Map<String, Object>> response = controller.getReports(ROLE_USER);
        assertEquals(403, response.getStatusCode().value());
    }

    // --- getAllUsers ---

    @Test
    void getAllUsers_returnsOkForAdmin() {
        when(adminService.getAllUsers()).thenReturn(List.of(Map.of("email", "user@test.com")));
        ResponseEntity<List<Map<String, Object>>> response = controller.getAllUsers(ROLE_ADMIN);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void getAllUsers_returns403ForNonAdmin() {
        ResponseEntity<List<Map<String, Object>>> response = controller.getAllUsers(ROLE_USER);
        assertEquals(403, response.getStatusCode().value());
    }

    // --- updateUser ---

    @Test
    void updateUser_returnsOkForAdmin() {
        UserUpdateRequest req = new UserUpdateRequest();
        req.setRole("ROLE_ADMIN");
        when(adminService.updateUser(1L, req)).thenReturn(Map.of("role", "ROLE_ADMIN"));
        ResponseEntity<Map<String, Object>> response = controller.updateUser(1L, ROLE_ADMIN, req);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void updateUser_returns403ForNonAdmin() {
        UserUpdateRequest req = new UserUpdateRequest();
        ResponseEntity<Map<String, Object>> response = controller.updateUser(1L, ROLE_USER, req);
        assertEquals(403, response.getStatusCode().value());
    }
}
