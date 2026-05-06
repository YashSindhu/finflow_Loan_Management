package com.example.documentservice.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.documentservice.dto.VerifyRequest;
import com.example.documentservice.entity.Document;
import com.example.documentservice.service.DocumentService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/documents")
public class DocumentController {

    private static final Logger log = LoggerFactory.getLogger(DocumentController.class);
    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_SUPER_ADMIN = "ROLE_SUPER_ADMIN";
    private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

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
        if (!isAdminRole(role)) {
            log.warn("PUT /documents/admin/{}/verify - access denied for role: {}", id, role);
            return ResponseEntity.status(403).build();
        }
        log.info("PUT /documents/admin/{}/verify - status: {}", id, request.getStatus());
        return ResponseEntity.ok(service.verify(id, request));
    }

    @GetMapping("/admin/all")
    public ResponseEntity<List<Document>> getAll(
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        if (!isAdminRole(role)) {
            log.warn("GET /documents/admin/all - access denied for role: {}", role);
            return ResponseEntity.status(403).build();
        }
        log.info("GET /documents/admin/all - by admin");
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}/view")
    public ResponseEntity<Resource> viewMyDocument(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Email", required = false) String email) throws IOException {
        Document doc = service.getById(id);
        if (!doc.getApplicantEmail().equals(email)) {
            return ResponseEntity.status(403).build();
        }
        return buildDocumentResponse(doc);
    }

    @GetMapping("/admin/{id}/view")
    public ResponseEntity<Resource> viewDocument(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Role", required = false) String role) throws IOException {
        if (!isAdminRole(role)) {
            log.warn("GET /documents/admin/{}/view - access denied for role: {}", id, role);
            return ResponseEntity.status(403).build();
        }
        Document doc = service.getById(id);
        log.info("GET /documents/admin/{}/view - serving file: {}", id, doc.getFileName());
        return buildDocumentResponse(doc);
    }

    private boolean isAdminRole(String role) {
        return ROLE_ADMIN.equals(role) || ROLE_SUPER_ADMIN.equals(role);
    }

    private ResponseEntity<Resource> buildDocumentResponse(Document doc) throws IOException {
        Path filePath = Paths.get(doc.getFilePath());
        Resource resource = new UrlResource(filePath.toUri());
        if (!resource.exists()) return ResponseEntity.notFound().build();
        String contentType = Files.probeContentType(filePath);
        if (contentType == null) contentType = DEFAULT_CONTENT_TYPE;
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + doc.getFileName() + "\"")
                .body(resource);
    }
}
