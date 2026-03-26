package com.example.applicationservice.service;

import com.example.applicationservice.dto.LoanApplicationRequest;
import com.example.applicationservice.entity.LoanApplication;
import com.example.applicationservice.entity.LoanApplication.ApplicationStatus;
import com.example.applicationservice.entity.OutboxEvent;
import com.example.applicationservice.repository.LoanApplicationRepository;
import com.example.applicationservice.repository.OutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanApplicationServiceTest {

    @Mock
    private LoanApplicationRepository repository;

    @Mock
    private OutboxEventRepository outboxRepository;

    @InjectMocks
    private LoanApplicationService service;

    private LoanApplication draftApp;
    private LoanApplicationRequest request;

    @BeforeEach
    void setUp() {
        draftApp = new LoanApplication();
        draftApp.setId(1L);
        draftApp.setApplicantEmail("user@test.com");
        draftApp.setStatus(ApplicationStatus.DRAFT);
        draftApp.setFullName("Yash Sindhu");
        draftApp.setLoanAmount(500000.0);
        draftApp.setTenureMonths(36);

        request = new LoanApplicationRequest();
        request.setFullName("Yash Sindhu");
        request.setPhone("9876543210");
        request.setLoanAmount(500000.0);
        request.setTenureMonths(36);
        request.setLoanPurpose("HOME");
        request.setEmploymentType("SALARIED");
        request.setMonthlyIncome(75000.0);
    }

    // --- create ---

    @Test
    void create_savesAndReturnsDraft() {
        when(repository.save(any())).thenReturn(draftApp);

        LoanApplication result = service.create("user@test.com", request);

        assertEquals(ApplicationStatus.DRAFT, result.getStatus());
        assertEquals("user@test.com", result.getApplicantEmail());
        verify(repository).save(any(LoanApplication.class));
    }

    // --- update ---

    @Test
    void update_successfullyUpdatesDraft() {
        when(repository.findById(1L)).thenReturn(Optional.of(draftApp));
        when(repository.save(any())).thenReturn(draftApp);

        LoanApplication result = service.update(1L, "user@test.com", request);

        assertNotNull(result);
        verify(repository).save(draftApp);
    }

    @Test
    void update_throwsIfNotDraft() {
        draftApp.setStatus(ApplicationStatus.SUBMITTED);
        when(repository.findById(1L)).thenReturn(Optional.of(draftApp));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.update(1L, "user@test.com", request));

        assertEquals("Only DRAFT applications can be updated", ex.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void update_throwsIfEmailDoesNotMatch() {
        when(repository.findById(1L)).thenReturn(Optional.of(draftApp));

        assertThrows(RuntimeException.class,
                () -> service.update(1L, "other@test.com", request));
    }

    // --- submit ---

    @Test
    void submit_setsStatusToSubmittedAndSavesOutboxEvent() {
        when(repository.findById(1L)).thenReturn(Optional.of(draftApp));
        when(repository.save(any())).thenReturn(draftApp);

        service.submit(1L, "user@test.com");

        assertEquals(ApplicationStatus.SUBMITTED, draftApp.getStatus());

        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxRepository).save(captor.capture());
        OutboxEvent event = captor.getValue();
        assertEquals("APPLICATION_SUBMITTED", event.getEventType());
        assertTrue(event.getPayload().contains("\"applicationId\":1"));
        assertFalse(event.isPublished());
    }

    @Test
    void submit_throwsIfNotDraft() {
        draftApp.setStatus(ApplicationStatus.SUBMITTED);
        when(repository.findById(1L)).thenReturn(Optional.of(draftApp));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.submit(1L, "user@test.com"));

        assertEquals("Only DRAFT applications can be submitted", ex.getMessage());
        verify(outboxRepository, never()).save(any());
    }

    @Test
    void submit_throwsIfEmailDoesNotMatch() {
        when(repository.findById(1L)).thenReturn(Optional.of(draftApp));

        assertThrows(RuntimeException.class,
                () -> service.submit(1L, "other@test.com"));

        verify(outboxRepository, never()).save(any());
    }

    // --- getMyApplications ---

    @Test
    void getMyApplications_returnsListForEmail() {
        when(repository.findByApplicantEmail("user@test.com")).thenReturn(List.of(draftApp));

        List<LoanApplication> result = service.getMyApplications("user@test.com");

        assertEquals(1, result.size());
        assertEquals("user@test.com", result.get(0).getApplicantEmail());
    }

    // --- getStatus ---

    @Test
    void getStatus_returnsApplicationForOwner() {
        when(repository.findById(1L)).thenReturn(Optional.of(draftApp));

        LoanApplication result = service.getStatus(1L, "user@test.com");

        assertEquals(1L, result.getId());
    }

    @Test
    void getStatus_throwsIfNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.getStatus(99L, "user@test.com"));
    }

    // --- updateStatus ---

    @Test
    void updateStatus_updatesAndReturnsApplication() {
        when(repository.findById(1L)).thenReturn(Optional.of(draftApp));
        when(repository.save(any())).thenReturn(draftApp);

        LoanApplication result = service.updateStatus(1L, ApplicationStatus.APPROVED);

        assertEquals(ApplicationStatus.APPROVED, result.getStatus());
        verify(repository).save(draftApp);
    }
}
