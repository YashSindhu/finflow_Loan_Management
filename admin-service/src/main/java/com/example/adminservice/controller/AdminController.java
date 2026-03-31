package com.example.adminservice.controller;

import com.example.adminservice.dto.DecisionRequest;
import com.example.adminservice.dto.UserUpdateRequest;
import com.example.adminservice.entity.Decision;
import com.example.adminservice.service.AdminService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);
    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/applications")
    public ResponseEntity<List<Map<String, Object>>> getAllApplications(
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        if (!ROLE_ADMIN.equals(role)) {
            log.warn("GET /admin/applications - access denied for role: {}", role);
            return ResponseEntity.status(403).build();
        }
        log.info("GET /admin/applications - by admin");
        return ResponseEntity.ok(adminService.getAllApplications());
    }

    @GetMapping("/applications/{id}")
    public ResponseEntity<Map<String, Object>> getApplication(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        if (!ROLE_ADMIN.equals(role)) {
            log.warn("GET /admin/applications/{} - access denied for role: {}", id, role);
            return ResponseEntity.status(403).build();
        }
        log.info("GET /admin/applications/{} - by admin", id);
        return ResponseEntity.ok(adminService.getApplicationById(id));
    }

    @PostMapping("/applications/{id}/decision")
    public ResponseEntity<Decision> makeDecision(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Email", required = false) String adminEmail,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @Valid @RequestBody DecisionRequest request) {
        if (!ROLE_ADMIN.equals(role)) {
            log.warn("POST /admin/applications/{}/decision - access denied for role: {}", id, role);
            return ResponseEntity.status(403).build();
        }
        log.info("POST /admin/applications/{}/decision - by admin: {}", id, adminEmail);
        return ResponseEntity.ok(adminService.makeDecision(id, adminEmail, request));
    }

    @GetMapping("/applications/{id}/decision")
    public ResponseEntity<Decision> getDecision(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        if (!ROLE_ADMIN.equals(role)) {
            log.warn("GET /admin/applications/{}/decision - access denied for role: {}", id, role);
            return ResponseEntity.status(403).build();
        }
        log.info("GET /admin/applications/{}/decision - by admin", id);
        return ResponseEntity.ok(adminService.getDecisionByApplication(id));
    }

    @GetMapping("/reports")
    public ResponseEntity<Map<String, Object>> getReports(
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        if (!ROLE_ADMIN.equals(role)) {
            log.warn("GET /admin/reports - access denied for role: {}", role);
            return ResponseEntity.status(403).build();
        }
        log.info("GET /admin/reports - by admin");
        return ResponseEntity.ok(adminService.getReports());
    }

    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> getAllUsers(
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        if (!ROLE_ADMIN.equals(role)) {
            log.warn("GET /admin/users - access denied for role: {}", role);
            return ResponseEntity.status(403).build();
        }
        log.info("GET /admin/users - by admin");
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> updateUser(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestBody UserUpdateRequest request) {
        if (!ROLE_ADMIN.equals(role)) {
            log.warn("PUT /admin/users/{} - access denied for role: {}", id, role);
            return ResponseEntity.status(403).build();
        }
        log.info("PUT /admin/users/{} - by admin", id);
        return ResponseEntity.ok(adminService.updateUser(id, request));
    }
}
