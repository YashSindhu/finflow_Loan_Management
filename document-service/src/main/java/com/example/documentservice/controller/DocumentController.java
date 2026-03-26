package com.example.documentservice.controller;

import com.example.documentservice.dto.VerifyRequest;
import com.example.documentservice.entity.Document;
import com.example.documentservice.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/documents")
public class DocumentController {

    @Autowired
    private DocumentService service;

    @Operation(summary = "Upload a document")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Document> upload(
            @RequestHeader(value = "X-User-Email", required = false) String email,
            @RequestParam("applicationId") Long applicationId,
            @RequestParam("documentType") String documentType,
            @RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(service.upload(email, applicationId, documentType, file));
    }

    // Applicant: get my documents
    @GetMapping("/my")
    public ResponseEntity<List<Document>> getMyDocuments(
            @RequestHeader(value = "X-User-Email", required = false) String email) {
        return ResponseEntity.ok(service.getMyDocuments(email));
    }

    // Get documents by application
    @GetMapping("/application/{applicationId}")
    public ResponseEntity<List<Document>> getByApplication(@PathVariable Long applicationId) {
        return ResponseEntity.ok(service.getByApplication(applicationId));
    }

    // Admin: verify or reject document
    @PutMapping("/admin/{id}/verify")
    public ResponseEntity<Document> verify(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @Valid @RequestBody VerifyRequest request) {
        if (!"ROLE_ADMIN".equals(role)) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(service.verify(id, request));
    }

    // Admin: get all documents
    @GetMapping("/admin/all")
    public ResponseEntity<List<Document>> getAll(
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        if (!"ROLE_ADMIN".equals(role)) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(service.getAll());
    }
}
