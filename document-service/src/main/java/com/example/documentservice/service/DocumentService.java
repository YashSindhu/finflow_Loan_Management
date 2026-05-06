package com.example.documentservice.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.documentservice.dto.VerifyRequest;
import com.example.documentservice.entity.Document;
import com.example.documentservice.entity.Document.DocumentStatus;
import com.example.documentservice.repository.DocumentRepository;

@Service
public class DocumentService {

    private static final Logger log = LoggerFactory.getLogger(DocumentService.class);

    @Autowired
    private DocumentRepository repository;

    @Value("${document.upload.dir}")
    private String uploadDir;

    public Document upload(String email, Long applicationId, String documentType, MultipartFile file) throws IOException {
        log.info("Uploading document - type: {}, applicationId: {}, email: {}", documentType, applicationId, email);
        Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
        if (!Files.exists(dir)) Files.createDirectories(dir);

        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            log.warn("Upload failed - invalid file name for email: {}", email);
            throw new IllegalArgumentException("Invalid file name");
        }
        Path fileNamePath = Paths.get(originalName).getFileName();
        if (fileNamePath == null) {
            throw new IllegalArgumentException("Invalid file name");
        }
        String safeName = fileNamePath.toString();
        String uniqueName = UUID.randomUUID() + "_" + safeName;
        Path filePath = dir.resolve(uniqueName).normalize();
        if (!filePath.startsWith(dir)) {
            log.warn("Upload failed - invalid file path detected for email: {}", email);
            throw new IllegalArgumentException("Invalid file path detected");
        }
        Files.copy(file.getInputStream(), filePath);

        Document doc = new Document();
        doc.setApplicantEmail(email);
        doc.setApplicationId(applicationId);
        doc.setDocumentType(documentType);
        doc.setFileName(originalName);
        doc.setFilePath(filePath.toString());
        Document saved = repository.save(doc);
        log.info("Document uploaded successfully - id: {}, type: {}, applicationId: {}", saved.getId(), documentType, applicationId);
        return saved;
    }

    public List<Document> getByApplication(Long applicationId) {
        log.debug("Fetching documents for applicationId: {}", applicationId);
        return repository.findByApplicationId(applicationId);
    }

    public List<Document> getMyDocuments(String email) {
        log.debug("Fetching documents for email: {}", email);
        return repository.findByApplicantEmail(email);
    }

    public Document verify(Long id, VerifyRequest req) {
        log.info("Verifying document id: {} with status: {}", id, req.getStatus());
        Document doc = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Document not found: " + id));
        doc.setStatus(DocumentStatus.valueOf(req.getStatus()));
        doc.setRemarks(req.getRemarks());
        doc.setVerifiedAt(LocalDateTime.now());
        Document saved = repository.save(doc);
        log.info("Document id: {} verified with status: {}", id, req.getStatus());
        return saved;
    }

    public List<Document> getAll() {
        log.debug("Fetching all documents");
        return repository.findAll();
    }

    public Document getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Document not found: " + id));
    }
}
