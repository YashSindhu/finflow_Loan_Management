package com.example.applicationservice.controller;

import com.example.applicationservice.dto.LoanApplicationRequest;
import com.example.applicationservice.entity.LoanApplication;
import com.example.applicationservice.service.LoanApplicationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/applications")
public class LoanApplicationController {

    @Autowired
    private LoanApplicationService service;

    // Applicant: create draft
    @PostMapping
    public ResponseEntity<LoanApplication> create(
            @RequestHeader(value = "X-User-Email", required = false) String email,
            @Valid @RequestBody LoanApplicationRequest request) {
        return ResponseEntity.ok(service.create(email, request));
    }

    // Applicant: update draft
    @PutMapping("/{id}")
    public ResponseEntity<LoanApplication> update(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Email", required = false) String email,
            @Valid @RequestBody LoanApplicationRequest request) {
        return ResponseEntity.ok(service.update(id, email, request));
    }

    // Applicant: submit application
    @PostMapping("/{id}/submit")
    public ResponseEntity<LoanApplication> submit(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Email", required = false) String email) {
        return ResponseEntity.ok(service.submit(id, email));
    }

    // Applicant: get my applications
    @GetMapping("/my")
    public ResponseEntity<List<LoanApplication>> getMyApplications(
            @RequestHeader(value = "X-User-Email", required = false) String email) {
        return ResponseEntity.ok(service.getMyApplications(email));
    }

    // Applicant: track status
    @GetMapping("/{id}/status")
    public ResponseEntity<LoanApplication> getStatus(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Email", required = false) String email) {
        return ResponseEntity.ok(service.getStatus(id, email));
    }

    // Admin: get all applications
    @GetMapping("/admin/all")
    public ResponseEntity<List<LoanApplication>> getAll(
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        if (!"ROLE_ADMIN".equals(role)) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(service.getAll());
    }

    // Admin: get single application
    @GetMapping("/admin/{id}")
    public ResponseEntity<LoanApplication> getById(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        if (!"ROLE_ADMIN".equals(role)) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(service.getById(id));
    }

    // Internal: called by admin-service to update status after decision
    @PutMapping("/admin/{id}/status")
    public ResponseEntity<LoanApplication> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        LoanApplication.ApplicationStatus status =
                LoanApplication.ApplicationStatus.valueOf(body.get("status"));
        return ResponseEntity.ok(service.updateStatus(id, status));
    }
}
