package com.example.adminservice.controller;

import com.example.adminservice.dto.DecisionRequest;
import com.example.adminservice.dto.UserUpdateRequest;
import com.example.adminservice.entity.Decision;
import com.example.adminservice.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    // ── Applications ──────────────────────────────────────────────

    @GetMapping("/applications")
    public ResponseEntity<List<Map<String, Object>>> getAllApplications(
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        if (!"ROLE_ADMIN".equals(role)) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(adminService.getAllApplications());
    }

    @GetMapping("/applications/{id}")
    public ResponseEntity<Map<String, Object>> getApplication(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        if (!"ROLE_ADMIN".equals(role)) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(adminService.getApplicationById(id));
    }

    // ── Decision ──────────────────────────────────────────────────

    @PostMapping("/applications/{id}/decision")
    public ResponseEntity<Decision> makeDecision(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Email", required = false) String adminEmail,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @Valid @RequestBody DecisionRequest request) {
        if (!"ROLE_ADMIN".equals(role)) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(adminService.makeDecision(id, adminEmail, request));
    }

    @GetMapping("/applications/{id}/decision")
    public ResponseEntity<Decision> getDecision(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        if (!"ROLE_ADMIN".equals(role)) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(adminService.getDecisionByApplication(id));
    }

    // ── Reports ───────────────────────────────────────────────────

    @GetMapping("/reports")
    public ResponseEntity<Map<String, Object>> getReports(
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        if (!"ROLE_ADMIN".equals(role)) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(adminService.getReports());
    }

    // ── Users ─────────────────────────────────────────────────────

    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> getAllUsers(
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        if (!"ROLE_ADMIN".equals(role)) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> updateUser(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestBody UserUpdateRequest request) {
        if (!"ROLE_ADMIN".equals(role)) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(adminService.updateUser(id, request));
    }
}
