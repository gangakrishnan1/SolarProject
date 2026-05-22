package com.lumenor.solariq.service;

import com.lumenor.solariq.dto.AssessmentRequestDTO;
import com.lumenor.solariq.dto.AssessmentResponseDTO;
import com.lumenor.solariq.dto.ContactCaptureDTO;
import com.lumenor.solariq.entity.Lead;
import com.lumenor.solariq.exception.NotFoundException;
import com.lumenor.solariq.repository.LeadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LeadServiceTest {

    private LeadRepository repo;
    private CalculatorService calculator;
    private AiService ai;
    private LeadService svc;

    @BeforeEach
    void setUp() {
        repo = mock(LeadRepository.class);
        calculator = mock(CalculatorService.class);
        ai = mock(AiService.class);
        svc = new LeadService(repo, calculator, ai);
    }

    private AssessmentRequestDTO req() {
        AssessmentRequestDTO r = new AssessmentRequestDTO();
        r.setCity("Hyderabad");
        r.setState("Telangana");
        r.setPropertyType("residential");
        r.setMonthlyBill(new BigDecimal("4500"));
        r.setRoofType("flat_rcc");
        r.setOwnership("own");
        return r;
    }

    private AssessmentResultData result() {
        return AssessmentResultData.builder()
                .systemSizeKw(2.5).installCost(125000).monthlySavings(3000)
                .annualSavings(36000).paybackYears(3.0).co2OffsetKg(2600)
                .subsidyAmount(35000).netCost(90000)
                .roofAreaRequiredSqft(250).roofAreaSufficient(true)
                .projection25yr(Collections.emptyList())
                .build();
    }

    @Test
    void createAssessment_savesAndReturnsDtoWithId() {
        when(calculator.runFullAssessment(any())).thenReturn(result());
        when(ai.getAiAnalysis(any(), any()))
                .thenReturn(new AiAnalysisResult("Great savings ahead.", List.of("a","b","c")));
        when(calculator.calculateLeadScore(any(), any())).thenReturn(72);
        UUID generated = UUID.randomUUID();
        when(repo.save(any(Lead.class))).thenAnswer(inv -> {
            Lead l = inv.getArgument(0);
            l.setId(generated);
            return l;
        });

        AssessmentResponseDTO dto = svc.createAssessment(req());

        verify(repo).save(any(Lead.class));
        assertNotNull(dto.getAssessmentId());
        assertEquals(generated.toString(), dto.getAssessmentId());
        assertEquals(72, dto.getLeadScore());
        assertEquals(3, dto.getAiTips().size());
    }

    @Test
    void captureContact_updatesEmail() {
        UUID id = UUID.randomUUID();
        Lead lead = new Lead();
        lead.setId(id);
        when(repo.findById(id)).thenReturn(Optional.of(lead));

        ContactCaptureDTO c = new ContactCaptureDTO();
        c.setEmail("new@example.com");
        svc.captureContact(id, c);

        ArgumentCaptor<Lead> captor = ArgumentCaptor.forClass(Lead.class);
        verify(repo).save(captor.capture());
        assertEquals("new@example.com", captor.getValue().getEmail());
    }

    @Test
    void captureContact_throwsNotFound() {
        UUID id = UUID.randomUUID();
        when(repo.findById(id)).thenReturn(Optional.empty());
        ContactCaptureDTO c = new ContactCaptureDTO();
        c.setEmail("a@b.com");
        assertThrows(NotFoundException.class, () -> svc.captureContact(id, c));
    }

    @Test
    void updateLeadStatus_changesStatus() {
        UUID id = UUID.randomUUID();
        Lead lead = new Lead();
        lead.setId(id);
        lead.setStatus("new");
        when(repo.findById(id)).thenReturn(Optional.of(lead));
        when(repo.save(any(Lead.class))).thenAnswer(inv -> inv.getArgument(0));

        Lead updated = svc.updateLeadStatus(id, "contacted", null, null);
        assertEquals("contacted", updated.getStatus());
    }

    @Test
    void updateLeadStatus_invalidValue_throws() {
        UUID id = UUID.randomUUID();
        assertThrows(IllegalArgumentException.class,
                () -> svc.updateLeadStatus(id, "flying", null, null));
    }
}
