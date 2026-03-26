package com.example.documentservice.controller;

import com.example.documentservice.dto.VerifyRequest;
import com.example.documentservice.entity.Document;
import com.example.documentservice.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(DocumentController.class);

    @Autowired
    private DocumentService service;

    @Operation(summary = "Upload a document")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Document> upload(
            @RequestHeader(value = "X-User-Email", required = false) String email,
            @RequestParam("applicationId") Long applicationId,
            @RequestParam("documentType") String documentType,
            @RequestParam("file") MultipartFile file) throws IOException {
        log.info("POST /documents/upload - email: {}, applicationId: {}, type: {}", email, applicationId, documentType);
        return ResponseEntity.ok(service.upload(email, applicationId, documentType, file));
    }

    @GetMapping("/my")
    public ResponseEntity<List<Document>> getMyDocuments(
            @RequestHeader(value = "X-User-Email", required = false) String email) {
        log.info("GET /documents/my - email: {}", email);
        return ResponseEntity.ok(service.getMyDocuments(email));
    }

    @GetMapping("/application/{applicationId}")
    public ResponseEntity<List<Document>> getByApplication(@PathVariable Long applicationId) {
        log.info("GET /documents/application/{}", applicationId);
        return ResponseEntity.ok(service.getByApplication(applicationId));
    }

    @PutMapping("/admin/{id}/verify")
    public ResponseEntity<Document> verify(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @Valid @RequestBody VerifyRequest request) {
        if (!"ROLE_ADMIN".equals(role)) {
            log.warn("PUT /documents/admin/{}/verify - access denied for role: {}", id, role);
            return ResponseEntity.status(403).build();
        }
        log.info("PUT /documents/admin/{}/verify - status: {}", id, request.getStatus());
        return ResponseEntity.ok(service.verify(id, request));
    }

    @GetMapping("/admin/all")
    public ResponseEntity<List<Document>> getAll(
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        if (!"ROLE_ADMIN".equals(role)) {
            log.warn("GET /documents/admin/all - access denied for role: {}", role);
            return ResponseEntity.status(403).build();
        }
        log.info("GET /documents/admin/all - by admin");
        return ResponseEntity.ok(service.getAll());
    }
}
