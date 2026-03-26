package com.example.applicationservice.service;

import com.example.applicationservice.dto.LoanApplicationRequest;
import com.example.applicationservice.entity.LoanApplication;
import com.example.applicationservice.entity.LoanApplication.ApplicationStatus;
import com.example.applicationservice.entity.OutboxEvent;
import com.example.applicationservice.repository.LoanApplicationRepository;
import com.example.applicationservice.repository.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class LoanApplicationService {

    private static final Logger log = LoggerFactory.getLogger(LoanApplicationService.class);

    @Autowired
    private LoanApplicationRepository repository;

    @Autowired
    private OutboxEventRepository outboxRepository;

    public LoanApplication create(String email, LoanApplicationRequest req) {
        log.info("Creating draft application for email: {}", email);
        LoanApplication app = new LoanApplication();
        app.setApplicantEmail(email);
        mapFields(app, req);
        LoanApplication saved = repository.save(app);
        log.info("Draft application created with id: {} for email: {}", saved.getId(), email);
        return saved;
    }

    public LoanApplication update(Long id, String email, LoanApplicationRequest req) {
        log.info("Updating application id: {} by email: {}", id, email);
        LoanApplication app = getOwned(id, email);
        if (app.getStatus() != ApplicationStatus.DRAFT) {
            log.warn("Update failed - application id: {} is not in DRAFT status", id);
            throw new RuntimeException("Only DRAFT applications can be updated");
        }
        mapFields(app, req);
        app.setUpdatedAt(LocalDateTime.now());
        LoanApplication saved = repository.save(app);
        log.info("Application id: {} updated successfully", id);
        return saved;
    }

    @Transactional
    public LoanApplication submit(Long id, String email) {
        log.info("Submitting application id: {} by email: {}", id, email);
        LoanApplication app = getOwned(id, email);
        if (app.getStatus() != ApplicationStatus.DRAFT) {
            log.warn("Submit failed - application id: {} is not in DRAFT status", id);
            throw new RuntimeException("Only DRAFT applications can be submitted");
        }
        app.setStatus(ApplicationStatus.SUBMITTED);
        app.setUpdatedAt(LocalDateTime.now());
        LoanApplication saved = repository.save(app);

        // Outbox pattern — save event in same transaction
        String payload = String.format(
                "{\"applicationId\":%d,\"applicantEmail\":\"%s\",\"event\":\"APPLICATION_SUBMITTED\"}",
                saved.getId(), saved.getApplicantEmail());
        outboxRepository.save(new OutboxEvent("APPLICATION_SUBMITTED", payload));
        log.info("Application id: {} submitted, outbox event saved", id);
        return saved;
    }

    public List<LoanApplication> getMyApplications(String email) {
        log.debug("Fetching applications for email: {}", email);
        return repository.findByApplicantEmail(email);
    }

    public LoanApplication getStatus(Long id, String email) {
        log.debug("Fetching status for application id: {} by email: {}", id, email);
        return getOwned(id, email);
    }

    public List<LoanApplication> getAll() {
        log.debug("Fetching all applications");
        return repository.findAll();
    }

    public LoanApplication getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found: " + id));
    }

    public LoanApplication updateStatus(Long id, ApplicationStatus status) {
        log.info("Updating status of application id: {} to: {}", id, status);
        LoanApplication app = getById(id);
        app.setStatus(status);
        app.setUpdatedAt(LocalDateTime.now());
        LoanApplication saved = repository.save(app);
        log.info("Application id: {} status updated to: {}", id, status);
        return saved;
    }

    private LoanApplication getOwned(Long id, String email) {
        LoanApplication app = getById(id);
        if (!app.getApplicantEmail().equals(email)) {
            throw new RuntimeException("Access denied");
        }
        return app;
    }

    private void mapFields(LoanApplication app, LoanApplicationRequest req) {
        Optional.ofNullable(req.getFullName()).ifPresent(app::setFullName);
        Optional.ofNullable(req.getPhone()).ifPresent(app::setPhone);
        Optional.ofNullable(req.getAddress()).ifPresent(app::setAddress);
        Optional.ofNullable(req.getDateOfBirth()).ifPresent(app::setDateOfBirth);
        Optional.ofNullable(req.getEmploymentType()).ifPresent(app::setEmploymentType);
        Optional.ofNullable(req.getEmployerName()).ifPresent(app::setEmployerName);
        Optional.ofNullable(req.getMonthlyIncome()).ifPresent(app::setMonthlyIncome);
        Optional.ofNullable(req.getLoanAmount()).ifPresent(app::setLoanAmount);
        Optional.ofNullable(req.getTenureMonths()).ifPresent(app::setTenureMonths);
        Optional.ofNullable(req.getLoanPurpose()).ifPresent(app::setLoanPurpose);
    }
}
