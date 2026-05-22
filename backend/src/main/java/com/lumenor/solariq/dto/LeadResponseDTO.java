package com.lumenor.solariq.dto;

import com.lumenor.solariq.entity.Lead;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Builder
public class LeadResponseDTO {
    private String id;
    private Instant createdAt;
    private Instant updatedAt;
    private String name;
    private String email;
    private String phone;
    private String city;
    private String state;
    private String propertyType;
    private BigDecimal monthlyBill;
    private BigDecimal monthlyUnits;
    private BigDecimal roofAreaSqft;
    private String roofType;
    private String ownership;
    private BigDecimal systemSizeKw;
    private BigDecimal installCost;
    private BigDecimal monthlySavings;
    private BigDecimal paybackYears;
    private BigDecimal co2OffsetKg;
    private BigDecimal subsidyAmount;
    private BigDecimal netCost;
    private String aiSummary;
    private List<String> aiTips;
    private Integer leadScore;
    private List<ProjectionYearDTO> projection25yr;
    private String status;
    private String assignedTo;
    private String notes;

    public static LeadResponseDTO from(Lead l, List<String> tips, List<ProjectionYearDTO> projection) {
        return LeadResponseDTO.builder()
                .id(l.getId().toString())
                .createdAt(l.getCreatedAt())
                .updatedAt(l.getUpdatedAt())
                .name(l.getName())
                .email(l.getEmail())
                .phone(l.getPhone())
                .city(l.getCity())
                .state(l.getState())
                .propertyType(l.getPropertyType())
                .monthlyBill(l.getMonthlyBill())
                .monthlyUnits(l.getMonthlyUnits())
                .roofAreaSqft(l.getRoofAreaSqft())
                .roofType(l.getRoofType())
                .ownership(l.getOwnership())
                .systemSizeKw(l.getSystemSizeKw())
                .installCost(l.getInstallCost())
                .monthlySavings(l.getMonthlySavings())
                .paybackYears(l.getPaybackYears())
                .co2OffsetKg(l.getCo2OffsetKg())
                .subsidyAmount(l.getSubsidyAmount())
                .netCost(l.getNetCost())
                .aiSummary(l.getAiSummary())
                .aiTips(tips)
                .leadScore(l.getLeadScore())
                .projection25yr(projection)
                .status(l.getStatus())
                .assignedTo(l.getAssignedTo())
                .notes(l.getNotes())
                .build();
    }
}
