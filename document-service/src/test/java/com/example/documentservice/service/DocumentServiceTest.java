package com.example.documentservice.service;

import com.example.documentservice.dto.VerifyRequest;
import com.example.documentservice.entity.Document;
import com.example.documentservice.entity.Document.DocumentStatus;
import com.example.documentservice.repository.DocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentRepository repository;

    @InjectMocks
    private DocumentService documentService;

    @TempDir
    Path tempDir;

    private Document document;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(documentService, "uploadDir", tempDir.toString());

        document = new Document();
        document.setId(1L);
        document.setApplicantEmail("user@test.com");
        document.setApplicationId(1L);
        document.setDocumentType("AADHAAR");
        document.setFileName("aadhaar.pdf");
        document.setStatus(DocumentStatus.PENDING);
    }

    // --- upload ---

    @Test
    void upload_savesDocumentAndReturnsIt() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "aadhaar.pdf",
                "application/pdf", "dummy content".getBytes());

        when(repository.save(any())).thenReturn(document);

        Document result = documentService.upload("user@test.com", 1L, "AADHAAR", file);

        assertNotNull(result);
        assertEquals("user@test.com", result.getApplicantEmail());
        verify(repository).save(any(Document.class));
    }

    @Test
    void upload_throwsForBlankFileName() {
        MockMultipartFile file = new MockMultipartFile("file", "", "application/pdf", "content".getBytes());

        assertThrows(RuntimeException.class,
                () -> documentService.upload("user@test.com", 1L, "AADHAAR", file));
        verify(repository, never()).save(any());
    }

    // --- getByApplication ---

    @Test
    void getByApplication_returnsDocumentsForApplicationId() {
        when(repository.findByApplicationId(1L)).thenReturn(List.of(document));

        List<Document> result = documentService.getByApplication(1L);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getApplicationId());
    }

    // --- getMyDocuments ---

    @Test
    void getMyDocuments_returnsDocumentsForEmail() {
        when(repository.findByApplicantEmail("user@test.com")).thenReturn(List.of(document));

        List<Document> result = documentService.getMyDocuments("user@test.com");

        assertEquals(1, result.size());
        assertEquals("user@test.com", result.get(0).getApplicantEmail());
    }

    // --- verify ---

    @Test
    void verify_updatesDocumentStatusToVerified() {
        VerifyRequest req = new VerifyRequest();
        req.setStatus("VERIFIED");
        req.setRemarks("All good");

        when(repository.findById(1L)).thenReturn(Optional.of(document));
        when(repository.save(any())).thenReturn(document);

        Document result = documentService.verify(1L, req);

        assertEquals(DocumentStatus.VERIFIED, result.getStatus());
        verify(repository).save(document);
    }

    @Test
    void verify_updatesDocumentStatusToRejected() {
        VerifyRequest req = new VerifyRequest();
        req.setStatus("REJECTED");
        req.setRemarks("Invalid document");

        when(repository.findById(1L)).thenReturn(Optional.of(document));
        when(repository.save(any())).thenReturn(document);

        documentService.verify(1L, req);

        assertEquals(DocumentStatus.REJECTED, document.getStatus());
    }

    @Test
    void verify_throwsIfDocumentNotFound() {
        VerifyRequest req = new VerifyRequest();
        req.setStatus("VERIFIED");

        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> documentService.verify(99L, req));
    }

    // --- getAll ---

    @Test
    void getAll_returnsAllDocuments() {
        when(repository.findAll()).thenReturn(List.of(document));

        List<Document> result = documentService.getAll();

        assertEquals(1, result.size());
    }
}
