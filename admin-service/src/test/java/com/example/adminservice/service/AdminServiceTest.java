package com.example.adminservice.service;

import com.example.adminservice.dto.DecisionRequest;
import com.example.adminservice.dto.UserUpdateRequest;
import com.example.adminservice.entity.Decision;
import com.example.adminservice.entity.Decision.DecisionType;
import com.example.adminservice.repository.DecisionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private DecisionRepository decisionRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private AdminService adminService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(adminService, "applicationServiceUrl", "http://application-service");
        ReflectionTestUtils.setField(adminService, "authServiceUrl", "http://auth-service");
    }

    // --- getAllApplications ---

    @Test
    void getAllApplications_returnsListFromApplicationService() {
        List<Map<String, Object>> apps = List.of(Map.of("id", 1));
        ResponseEntity<List<Map<String, Object>>> response = ResponseEntity.ok(apps);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class)))
                .thenReturn(response);

        List<Map<String, Object>> result = adminService.getAllApplications();

        assertEquals(1, result.size());
    }

    // --- getApplicationById ---

    @Test
    void getApplicationById_returnsApplicationFromService() {
        Map<String, Object> app = Map.of("id", 1L);
        ResponseEntity<Map<String, Object>> response = ResponseEntity.ok(app);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class)))
                .thenReturn(response);

        Map<String, Object> result = adminService.getApplicationById(1L);

        assertEquals(1L, result.get("id"));
    }

    // --- makeDecision ---

    @Test
    void makeDecision_savesDecisionAndUpdatesApplicationStatus() {
        DecisionRequest req = new DecisionRequest();
        req.setDecision("APPROVED");
        req.setRemarks("Looks good");
        req.setApprovedAmount(500000.0);
        req.setInterestRate(10.5);
        req.setTenureMonths(36);

        Decision saved = new Decision();
        saved.setId(1L);
        saved.setApplicationId(1L);
        saved.setDecision(DecisionType.APPROVED);

        when(decisionRepository.findByApplicationId(1L)).thenReturn(Optional.empty());
        when(decisionRepository.save(any())).thenReturn(saved);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.PUT), any(), eq(Void.class)))
                .thenReturn(ResponseEntity.ok().build());

        Decision result = adminService.makeDecision(1L, "admin@test.com", req);

        assertEquals(DecisionType.APPROVED, result.getDecision());
        verify(decisionRepository).save(any(Decision.class));
    }

    @Test
    void makeDecision_throwsIfDecisionAlreadyExists() {
        DecisionRequest req = new DecisionRequest();
        req.setDecision("APPROVED");

        when(decisionRepository.findByApplicationId(1L)).thenReturn(Optional.of(new Decision()));

        assertThrows(RuntimeException.class, () -> adminService.makeDecision(1L, "admin@test.com", req));
        verify(decisionRepository, never()).save(any());
    }

    // --- getDecisionByApplication ---

    @Test
    void getDecisionByApplication_returnsDecision() {
        Decision decision = new Decision();
        decision.setApplicationId(1L);
        when(decisionRepository.findByApplicationId(1L)).thenReturn(Optional.of(decision));

        Decision result = adminService.getDecisionByApplication(1L);

        assertEquals(1L, result.getApplicationId());
    }

    @Test
    void getDecisionByApplication_throwsIfNotFound() {
        when(decisionRepository.findByApplicationId(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> adminService.getDecisionByApplication(99L));
    }

    // --- getReports ---

    @Test
    void getReports_returnsCorrectCounts() {
        List<Map<String, Object>> apps = List.of(Map.of("id", 1), Map.of("id", 2), Map.of("id", 3));
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(apps));

        Decision approved = new Decision(); approved.setDecision(DecisionType.APPROVED);
        Decision rejected = new Decision(); rejected.setDecision(DecisionType.REJECTED);
        when(decisionRepository.findByDecision(DecisionType.APPROVED)).thenReturn(List.of(approved));
        when(decisionRepository.findByDecision(DecisionType.REJECTED)).thenReturn(List.of(rejected));

        Map<String, Object> report = adminService.getReports();

        assertEquals(3L, report.get("totalApplications"));
        assertEquals(1L, report.get("approved"));
        assertEquals(1L, report.get("rejected"));
        assertEquals(1L, report.get("pending"));
    }

    // --- getAllUsers ---

    @Test
    void getAllUsers_returnsListFromAuthService() {
        List<Map<String, Object>> users = List.of(Map.of("email", "user@test.com"));
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(users));

        List<Map<String, Object>> result = adminService.getAllUsers();

        assertEquals(1, result.size());
    }

    // --- updateUser ---

    @Test
    void updateUser_returnsUpdatedUserFromAuthService() {
        Map<String, Object> updated = Map.of("email", "user@test.com", "role", "ROLE_ADMIN");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.PUT), any(), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(updated));

        UserUpdateRequest req = new UserUpdateRequest();
        req.setRole("ROLE_ADMIN");

        Map<String, Object> result = adminService.updateUser(1L, req);

        assertEquals("ROLE_ADMIN", result.get("role"));
    }
}
