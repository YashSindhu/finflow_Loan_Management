package com.example.adminservice.service;

import com.example.adminservice.dto.DecisionRequest;
import com.example.adminservice.dto.UserUpdateRequest;
import com.example.adminservice.entity.Decision;
import com.example.adminservice.entity.Decision.DecisionType;
import com.example.adminservice.repository.DecisionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminService.class);

    private final DecisionRepository decisionRepository;
    private final RestTemplate restTemplate;

    @Value("${application.service.url}")
    private String applicationServiceUrl;

    @Value("${auth.service.url}")
    private String authServiceUrl;

    public AdminService(DecisionRepository decisionRepository, RestTemplate restTemplate) {
        this.decisionRepository = decisionRepository;
        this.restTemplate = restTemplate;
    }

    private HttpEntity<Void> adminHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-Role", "ROLE_ADMIN");
        return new HttpEntity<>(headers);
    }

    private <T> HttpEntity<T> adminHeadersWith(T body) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-Role", "ROLE_ADMIN");
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }

    public List<Map<String, Object>> getAllApplications() {
        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                applicationServiceUrl + "/applications/admin/all",
                HttpMethod.GET,
                adminHeaders(),
                new ParameterizedTypeReference<>() {}
        );
        return response.getBody();
    }

    public Map<String, Object> getApplicationById(Long id) {
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                applicationServiceUrl + "/applications/admin/" + id,
                HttpMethod.GET,
                adminHeaders(),
                new ParameterizedTypeReference<>() {}
        );
        return response.getBody();
    }

    public Decision makeDecision(Long applicationId, String adminEmail, DecisionRequest req) {
        log.info("Admin: {} making decision: {} for application id: {}", adminEmail, req.getDecisionType(), applicationId);
        if (decisionRepository.findByApplicationId(applicationId).isPresent()) {
            log.warn("Decision already exists for application id: {}", applicationId);
            throw new IllegalStateException("Decision already made for application: " + applicationId);
        }

        Decision decision = new Decision();
        decision.setApplicationId(applicationId);
        decision.setAdminEmail(adminEmail);
        decision.setDecision(DecisionType.valueOf(req.getDecisionType()));
        decision.setRemarks(req.getRemarks());
        decision.setApprovedAmount(req.getApprovedAmount());
        decision.setInterestRate(req.getInterestRate());
        decision.setTenureMonths(req.getTenureMonths());
        Decision saved = decisionRepository.save(decision);

        String newStatus = DecisionType.APPROVED.name().equals(req.getDecisionType()) ? "APPROVED" : "REJECTED";
        log.info("Updating application id: {} status to: {}", applicationId, newStatus);
        updateApplicationStatus(applicationId, newStatus);
        log.info("Decision saved successfully for application id: {}", applicationId);
        return saved;
    }

    private void updateApplicationStatus(Long applicationId, String status) {
        String url = applicationServiceUrl + "/applications/admin/" + applicationId + "/status";
        Map<String, String> body = Map.of("status", status);
        restTemplate.exchange(url, HttpMethod.PUT, adminHeadersWith(body), Void.class);
    }

    public Decision getDecisionByApplication(Long applicationId) {
        return decisionRepository.findByApplicationId(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("No decision found for application: " + applicationId));
    }

    public Map<String, Object> getReports() {
        List<Map<String, Object>> allApps = getAllApplications();
        long total = allApps == null ? 0 : allApps.size();

        long approved = decisionRepository.findByDecision(DecisionType.APPROVED).size();
        long rejected = decisionRepository.findByDecision(DecisionType.REJECTED).size();
        long pending = total - approved - rejected;

        Map<String, Object> report = new HashMap<>();
        report.put("totalApplications", total);
        report.put("approved", approved);
        report.put("rejected", rejected);
        report.put("pending", pending);
        report.put("approvalRate", total > 0 ? (approved * 100.0 / total) + "%" : "0%");
        return report;
    }

    public List<Map<String, Object>> getAllUsers() {
        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                authServiceUrl + "/auth/admin/users",
                HttpMethod.GET,
                adminHeaders(),
                new ParameterizedTypeReference<>() {}
        );
        return response.getBody();
    }

    public Map<String, Object> updateUser(Long userId, UserUpdateRequest req) {
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                authServiceUrl + "/auth/admin/users/" + userId,
                HttpMethod.PUT,
                adminHeadersWith(req),
                new ParameterizedTypeReference<>() {}
        );
        return response.getBody();
    }
}
