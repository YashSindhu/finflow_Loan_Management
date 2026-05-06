package com.example.applicationservice.controller;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.applicationservice.dto.LoanApplicationRequest;
import com.example.applicationservice.entity.LoanApplication;
import com.example.applicationservice.service.LoanApplicationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/applications")
public class LoanApplicationController {

    private static final Logger log = LoggerFactory.getLogger(LoanApplicationController.class);

    @Autowired
    private LoanApplicationService service;

    @PostMapping
    public ResponseEntity<LoanApplication> create(
            @RequestHeader(value = "X-User-Email", required = false) String email,
            @Valid @RequestBody LoanApplicationRequest request) {
        log.info("POST /applications - create draft for email: {}", email);
        return ResponseEntity.ok(service.create(email, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LoanApplication> update(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Email", required = false) String email,
            @Valid @RequestBody LoanApplicationRequest request) {
        log.info("PUT /applications/{} - update by email: {}", id, email);
        return ResponseEntity.ok(service.update(id, email, request));
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<LoanApplication> submit(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Email", required = false) String email) {
        log.info("POST /applications/{}/submit - by email: {}", id, email);
        return ResponseEntity.ok(service.submit(id, email));
    }

    @GetMapping("/my")
    public ResponseEntity<List<LoanApplication>> getMyApplications(
            @RequestHeader(value = "X-User-Email", required = false) String email) {
        log.info("GET /applications/my - email: {}", email);
        return ResponseEntity.ok(service.getMyApplications(email));
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<LoanApplication> getStatus(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Email", required = false) String email) {
        log.info("GET /applications/{}/status - email: {}", id, email);
        return ResponseEntity.ok(service.getStatus(id, email));
    }

    @GetMapping("/admin/all")
    public ResponseEntity<List<LoanApplication>> getAll(
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        if (!"ROLE_ADMIN".equals(role) && !"ROLE_SUPER_ADMIN".equals(role)) {
            log.warn("GET /applications/admin/all - access denied for role: {}", role);
            return ResponseEntity.status(403).build();
        }
        log.info("GET /applications/admin/all - by admin");
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/admin/{id}")
    public ResponseEntity<LoanApplication> getById(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        if (!"ROLE_ADMIN".equals(role) && !"ROLE_SUPER_ADMIN".equals(role)) {
            log.warn("GET /applications/admin/{} - access denied for role: {}", id, role);
            return ResponseEntity.status(403).build();
        }
        log.info("GET /applications/admin/{} - by admin", id);
        return ResponseEntity.ok(service.getById(id));
    }

    @PutMapping("/admin/{id}/status")
    public ResponseEntity<LoanApplication> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        log.info("PUT /applications/admin/{}/status - new status: {}", id, body.get("status"));
        LoanApplication.ApplicationStatus status =
                LoanApplication.ApplicationStatus.valueOf(body.get("status"));
        return ResponseEntity.ok(service.updateStatus(id, status));
    }
}
