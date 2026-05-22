package com.lumenor.solariq.service;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class AssessmentResultData {
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
    private List<ProjectionYear> projection25yr;
    private double idealSystemSizeKw;
    private PanelRecommendation panelRecommendation;
}
