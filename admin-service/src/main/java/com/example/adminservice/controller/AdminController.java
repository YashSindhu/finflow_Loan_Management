package com.example.adminservice.controller;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.adminservice.dto.DecisionRequest;
import com.example.adminservice.dto.UserUpdateRequest;
import com.example.adminservice.entity.Decision;
import com.example.adminservice.service.AdminService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // Both ROLE_ADMIN and ROLE_SUPER_ADMIN have admin access
    private boolean isAdmin(String role) {
        return "ROLE_ADMIN".equals(role) || "ROLE_SUPER_ADMIN".equals(role);
    }

    @GetMapping("/applications")
    public ResponseEntity<List<Map<String, Object>>> getAllApplications(
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        if (!isAdmin(role)) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(adminService.getAllApplications());
    }

    @GetMapping("/applications/{id}")
    public ResponseEntity<Map<String, Object>> getApplication(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        if (!isAdmin(role)) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(adminService.getApplicationById(id));
    }

    @PostMapping("/applications/{id}/decision")
    public ResponseEntity<Decision> makeDecision(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Email", required = false) String adminEmail,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @Valid @RequestBody DecisionRequest request) {
        if (!isAdmin(role)) return ResponseEntity.status(403).build();
        log.info("POST /admin/applications/{}/decision - by: {}", id, adminEmail);
        return ResponseEntity.ok(adminService.makeDecision(id, adminEmail, request));
    }

    @GetMapping("/applications/{id}/decision")
    public ResponseEntity<Decision> getDecision(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        if (!isAdmin(role)) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(adminService.getDecisionByApplication(id));
    }

    @GetMapping("/reports")
    public ResponseEntity<Map<String, Object>> getReports(
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        if (!isAdmin(role)) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(adminService.getReports());
    }

    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> getAllUsers(
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        if (!isAdmin(role)) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> updateUser(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestBody UserUpdateRequest request) {
        if (!isAdmin(role)) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(adminService.updateUser(id, request));
    }
}
