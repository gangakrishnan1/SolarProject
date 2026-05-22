package com.lumenor.solariq.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lumenor.solariq.dto.AssessmentRequestDTO;
import com.lumenor.solariq.dto.AssessmentResponseDTO;
import com.lumenor.solariq.dto.ContactCaptureDTO;
import com.lumenor.solariq.dto.ProjectionYearDTO;
import com.lumenor.solariq.entity.Lead;
import com.lumenor.solariq.exception.NotFoundException;
import com.lumenor.solariq.repository.LeadRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class LeadService {

    private static final Set<String> ALLOWED_STATUSES = Set.of(
            "new", "contacted", "site_visit", "converted", "lost"
    );

    private final LeadRepository repo;
    private final CalculatorService calculator;
    private final AiService aiService;
    private final ObjectMapper mapper = new ObjectMapper();

    public LeadService(LeadRepository repo, CalculatorService calculator, AiService aiService) {
        this.repo = repo;
        this.calculator = calculator;
        this.aiService = aiService;
    }

    @Transactional
    public AssessmentResponseDTO createAssessment(AssessmentRequestDTO req) {
        AssessmentResultData result = calculator.runFullAssessment(req);
        AiAnalysisResult ai = aiService.getAiAnalysis(req, result);
        int leadScore = calculator.calculateLeadScore(req, result);

        Lead lead = new Lead();
        lead.setCity(req.getCity());
        lead.setState(req.getState());
        lead.setPropertyType(req.getPropertyType());
        lead.setMonthlyBill(req.getMonthlyBill());
        lead.setMonthlyUnits(req.getMonthlyUnits());
        lead.setRoofAreaSqft(req.getRoofAreaSqft());
        lead.setRoofType(req.getRoofType());
        lead.setOwnership(req.getOwnership());

        lead.setSystemSizeKw(BigDecimal.valueOf(result.getSystemSizeKw()));
        lead.setInstallCost(BigDecimal.valueOf(result.getInstallCost()));
        lead.setMonthlySavings(BigDecimal.valueOf(result.getMonthlySavings()));
        lead.setPaybackYears(BigDecimal.valueOf(result.getPaybackYears()));
        lead.setCo2OffsetKg(BigDecimal.valueOf(result.getCo2OffsetKg()));
        lead.setSubsidyAmount(BigDecimal.valueOf(result.getSubsidyAmount()));
        lead.setNetCost(BigDecimal.valueOf(result.getNetCost()));

        lead.setAiSummary(ai.getSummary());
        lead.setLeadScore(leadScore);
        lead.setAiTips(toJson(ai.getTips()));
        lead.setProjection25yr(toJson(result.getProjection25yr()));
        lead.setStatus("new");

        Lead saved = repo.save(lead);

        PanelRecommendation panelRec = result.getPanelRecommendation();

        return AssessmentResponseDTO.builder()
                .assessmentId(saved.getId().toString())
                .systemSizeKw(result.getSystemSizeKw())
                .idealSystemSizeKw(panelRec != null ? panelRec.getIdealSystemSizeKw() : result.getSystemSizeKw())
                .recommendedSystemSizeKw(panelRec != null ? panelRec.getRecommendedSystemSizeKw() : result.getSystemSizeKw())
                .recommendedPanelCount(panelRec != null ? panelRec.getPanelCount() : 0)
                .panelWattage(panelRec != null ? panelRec.getPanelWattage() : 540)
                .panelRecommendationSummary(panelRec != null ? panelRec.getSummary() : null)
                .areaType(panelRec != null ? panelRec.getAreaType() : req.getAreaType())
                .areaLabel(panelRec != null ? panelRec.getAreaLabel() : null)
                .sizeCappedForArea(panelRec != null && panelRec.isSizeCappedForArea())
                .installCost(result.getInstallCost())
                .monthlySavings(result.getMonthlySavings())
                .annualSavings(result.getAnnualSavings())
                .paybackYears(result.getPaybackYears())
                .co2OffsetKg(result.getCo2OffsetKg())
                .subsidyAmount(result.getSubsidyAmount())
                .netCost(result.getNetCost())
                .roofAreaRequiredSqft(result.getRoofAreaRequiredSqft())
                .roofAreaSufficient(result.isRoofAreaSufficient())
                .aiSummary(ai.getSummary())
                .aiTips(ai.getTips())
                .leadScore(leadScore)
                .projection25yr(toProjectionDTOs(result.getProjection25yr()))
                .build();
    }

    @Transactional
    public void captureContact(UUID id, ContactCaptureDTO dto) {
        Lead lead = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Lead not found: " + id));
        if (dto.getName() != null && !dto.getName().isBlank()) lead.setName(dto.getName());
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) lead.setEmail(dto.getEmail());
        if (dto.getPhone() != null && !dto.getPhone().isBlank()) lead.setPhone(dto.getPhone());
        repo.save(lead);
    }

    public List<Lead> getLeads(String status, String state, Integer minScore) {
        if (status != null && !status.isBlank()) {
            return repo.findByStatusOrderByLeadScoreDesc(status);
        }
        if (state != null && !state.isBlank()) {
            return repo.findByStateContainingIgnoreCaseOrderByLeadScoreDesc(state);
        }
        if (minScore != null) {
            return repo.findByLeadScoreGreaterThanEqualOrderByLeadScoreDesc(minScore);
        }
        return repo.findAllByOrderByLeadScoreDesc();
    }

    @Transactional
    public Lead updateLeadStatus(UUID id, String newStatus, String notes, String assignedTo) {
        if (newStatus == null || !ALLOWED_STATUSES.contains(newStatus)) {
            throw new IllegalArgumentException("Invalid status: " + newStatus
                    + ". Allowed: " + ALLOWED_STATUSES);
        }
        Lead lead = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Lead not found: " + id));
        lead.setStatus(newStatus);
        if (notes != null) lead.setNotes(notes);
        if (assignedTo != null) lead.setAssignedTo(assignedTo);
        return repo.save(lead);
    }

    private String toJson(Object o) {
        try {
            return mapper.writeValueAsString(o);
        } catch (Exception e) {
            return "[]";
        }
    }

    public List<String> tipsFromJson(String json) {
        try {
            if (json == null || json.isBlank()) return Collections.emptyList();
            return mapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public List<ProjectionYearDTO> projectionFromJson(String json) {
        try {
            if (json == null || json.isBlank()) return Collections.emptyList();
            return mapper.readValue(json, new TypeReference<List<ProjectionYearDTO>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private List<ProjectionYearDTO> toProjectionDTOs(List<ProjectionYear> source) {
        List<ProjectionYearDTO> out = new ArrayList<>(source.size());
        for (ProjectionYear py : source) {
            out.add(new ProjectionYearDTO(py.getYear(), py.getAnnualSavings(),
                    py.getCumulativeSavings(), py.getNetPosition()));
        }
        return out;
    }
}
