package com.lumenor.solariq.controller;

import com.lumenor.solariq.dto.*;
import com.lumenor.solariq.entity.Lead;
import com.lumenor.solariq.exception.NotFoundException;
import com.lumenor.solariq.service.LeadService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
public class AssessmentController {

    private final LeadService leadService;

    public AssessmentController(LeadService leadService) {
        this.leadService = leadService;
    }

    @PostMapping("/assess")
    public ResponseEntity<AssessmentResponseDTO> assess(@Valid @RequestBody AssessmentRequestDTO req) {
        AssessmentResponseDTO resp = leadService.createAssessment(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @PostMapping("/assess/{id}/capture")
    public ResponseEntity<Map<String, Object>> capture(
            @PathVariable("id") UUID id,
            @Valid @RequestBody ContactCaptureDTO dto) {
        if (!dto.hasAtLeastOneContact()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "validation_failed",
                    "message", "At least one of email or phone must be provided"
            ));
        }
        leadService.captureContact(id, dto);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Contact captured. Report will be sent shortly."
        ));
    }

    @GetMapping("/leads")
    public ResponseEntity<List<LeadResponseDTO>> listLeads(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) Integer minScore) {
        List<Lead> leads = leadService.getLeads(status, state, minScore);
        List<LeadResponseDTO> dtos = leads.stream()
                .map(l -> LeadResponseDTO.from(l,
                        leadService.tipsFromJson(l.getAiTips()),
                        leadService.projectionFromJson(l.getProjection25yr())))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PatchMapping("/leads/{id}/status")
    public ResponseEntity<LeadResponseDTO> updateStatus(
            @PathVariable("id") UUID id,
            @Valid @RequestBody StatusUpdateDTO dto) {
        Lead updated = leadService.updateLeadStatus(id, dto.getStatus(), dto.getNotes(), dto.getAssignedTo());
        return ResponseEntity.ok(LeadResponseDTO.from(updated,
                leadService.tipsFromJson(updated.getAiTips()),
                leadService.projectionFromJson(updated.getProjection25yr())));
    }
}
