package com.lumenor.solariq.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class AssessmentResponseDTO {
    private String assessmentId;
    private double systemSizeKw;
    private long installCost;
    private long monthlySavings;
    private long annualSavings;
    private double paybackYears;
    private long co2OffsetKg;
    private long subsidyAmount;
    private long netCost;
    private int roofAreaRequiredSqft;
    private boolean roofAreaSufficient;
    private String aiSummary;
    private List<String> aiTips;
    private int leadScore;
    private List<ProjectionYearDTO> projection25yr;
    private String areaType;
    private String areaLabel;
    private double idealSystemSizeKw;
    private double recommendedSystemSizeKw;
    private int recommendedPanelCount;
    private int panelWattage;
    private String panelRecommendationSummary;
    private boolean sizeCappedForArea;
}
