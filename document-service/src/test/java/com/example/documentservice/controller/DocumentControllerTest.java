package com.example.documentservice.controller;

import com.example.documentservice.dto.VerifyRequest;
import com.example.documentservice.entity.Document;
import com.example.documentservice.entity.Document.DocumentStatus;
import com.example.documentservice.service.DocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentControllerTest {

    @Mock
    private DocumentService service;

    private DocumentController controller;

    private Document document;

    @BeforeEach
    void setUp() {
        controller = new DocumentController();
        ReflectionTestUtils.setField(controller, "service", service);

        document = new Document();
        document.setId(1L);
        document.setApplicantEmail("user@test.com");
        document.setApplicationId(1L);
        document.setStatus(DocumentStatus.PENDING);
    }

    @Test
    void upload_returnsOk() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "aadhaar.pdf",
                "application/pdf", "content".getBytes());
        when(service.upload("user@test.com", 1L, "AADHAAR", file)).thenReturn(document);

        ResponseEntity<Document> response = controller.upload("user@test.com", 1L, "AADHAAR", file);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }

    @Test
    void getMyDocuments_returnsOk() {
        when(service.getMyDocuments("user@test.com")).thenReturn(List.of(document));
        ResponseEntity<List<Document>> response = controller.getMyDocuments("user@test.com");
        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void getByApplication_returnsOk() {
        when(service.getByApplication(1L)).thenReturn(List.of(document));
        ResponseEntity<List<Document>> response = controller.getByApplication(1L);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void verify_returnsOkForAdmin() {
        VerifyRequest req = new VerifyRequest();
        req.setStatus("VERIFIED");
        document.setStatus(DocumentStatus.VERIFIED);
        when(service.verify(1L, req)).thenReturn(document);

        ResponseEntity<Document> response = controller.verify(1L, "ROLE_ADMIN", req);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(DocumentStatus.VERIFIED, response.getBody().getStatus());
    }

    @Test
    void verify_returns403ForNonAdmin() {
        VerifyRequest req = new VerifyRequest();
        ResponseEntity<Document> response = controller.verify(1L, "ROLE_USER", req);
        assertEquals(403, response.getStatusCode().value());
        verify(service, never()).verify(anyLong(), any());
    }

    @Test
    void getAll_returnsOkForAdmin() {
        when(service.getAll()).thenReturn(List.of(document));
        ResponseEntity<List<Document>> response = controller.getAll("ROLE_ADMIN");
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void getAll_returns403ForNonAdmin() {
        ResponseEntity<List<Document>> response = controller.getAll("ROLE_USER");
        assertEquals(403, response.getStatusCode().value());
        verify(service, never()).getAll();
    }

    @Test
    void viewMyDocument_returnsFileForOwner() throws IOException {
        Path file = Files.createTempFile("document-owner", ".txt");
        Files.writeString(file, "loan document");
        document.setFilePath(file.toString());
        document.setFileName("document-owner.txt");
        when(service.getById(1L)).thenReturn(document);

        ResponseEntity<Resource> response = controller.viewMyDocument(1L, "user@test.com");

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("inline; filename=\"document-owner.txt\"",
                response.getHeaders().getFirst("Content-Disposition"));
        Files.deleteIfExists(file);
    }

    @Test
    void viewMyDocument_returns403ForDifferentUser() throws IOException {
        when(service.getById(1L)).thenReturn(document);

        ResponseEntity<Resource> response = controller.viewMyDocument(1L, "other@test.com");

        assertEquals(403, response.getStatusCode().value());
    }

    @Test
    void viewMyDocument_returns404WhenFileMissing() throws IOException {
        document.setFilePath(Path.of("missing-document.pdf").toAbsolutePath().toString());
        document.setFileName("missing-document.pdf");
        when(service.getById(1L)).thenReturn(document);

        ResponseEntity<Resource> response = controller.viewMyDocument(1L, "user@test.com");

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void viewDocument_returnsFileForAdmin() throws IOException {
        Path file = Files.createTempFile("document-admin", ".txt");
        Files.writeString(file, "loan document");
        document.setFilePath(file.toString());
        document.setFileName("document-admin.txt");
        when(service.getById(1L)).thenReturn(document);

        ResponseEntity<Resource> response = controller.viewDocument(1L, "ROLE_ADMIN");

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("inline; filename=\"document-admin.txt\"",
                response.getHeaders().getFirst("Content-Disposition"));
        Files.deleteIfExists(file);
    }

    @Test
    void viewDocument_returns403ForNonAdmin() throws IOException {
        ResponseEntity<Resource> response = controller.viewDocument(1L, "ROLE_USER");

        assertEquals(403, response.getStatusCode().value());
        verify(service, never()).getById(anyLong());
    }

    @Test
    void viewDocument_returns404WhenFileMissing() throws IOException {
        document.setFilePath(Path.of("missing-admin-document.pdf").toAbsolutePath().toString());
        document.setFileName("missing-admin-document.pdf");
        when(service.getById(1L)).thenReturn(document);

        ResponseEntity<Resource> response = controller.viewDocument(1L, "ROLE_ADMIN");

        assertEquals(404, response.getStatusCode().value());
    }
}
