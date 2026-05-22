package com.lumenor.solariq.service;

import com.lumenor.solariq.dto.AssessmentRequestDTO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CalculatorService {

    private static final double EFFICIENCY = 0.80;
    private static final double CO2_PER_KWH = 0.82;
    private static final double DAYS_PER_MONTH = 30.0;
    private static final double COMMERCIAL_TARIFF_MULTIPLIER = 1.4;
    private static final double INDUSTRIAL_TARIFF_MULTIPLIER = 1.6;
    private static final double ROOF_SQFT_PER_KW = 100.0;
    private static final double TARIFF_ESCALATION_PCT = 0.03;
    private static final double DEGRADATION_PCT = 0.005;

    private static final Map<String, StateConfig> STATE_CONFIGS = new HashMap<>();
    private static final Map<String, AreaProfile> AREA_PROFILES = new HashMap<>();
    private static final int DEFAULT_PANEL_WATTAGE = 540;

    static {
        AREA_PROFILES.put("rural", new AreaProfile("rural", "Rural", 5.0, 450));
        AREA_PROFILES.put("semi_urban", new AreaProfile("semi_urban", "Semi-Urban", 8.0, 540));
        AREA_PROFILES.put("urban", new AreaProfile("urban", "Urban", 12.0, 540));
        AREA_PROFILES.put("metro", new AreaProfile("metro", "Metro", 20.0, 550));
        STATE_CONFIGS.put("Telangana", new StateConfig("Telangana", 7.50, 5.2, 50_000));
        STATE_CONFIGS.put("Andhra Pradesh", new StateConfig("Andhra Pradesh", 7.30, 5.3, 50_000));
        STATE_CONFIGS.put("Karnataka", new StateConfig("Karnataka", 7.80, 5.5, 52_000));
        STATE_CONFIGS.put("Tamil Nadu", new StateConfig("Tamil Nadu", 7.20, 5.4, 51_000));
        STATE_CONFIGS.put("Kerala", new StateConfig("Kerala", 6.50, 4.8, 52_000));
        STATE_CONFIGS.put("Maharashtra", new StateConfig("Maharashtra", 8.50, 5.0, 53_000));
        STATE_CONFIGS.put("Gujarat", new StateConfig("Gujarat", 7.00, 5.7, 50_000));
        STATE_CONFIGS.put("Rajasthan", new StateConfig("Rajasthan", 7.10, 5.8, 50_000));
        STATE_CONFIGS.put("Delhi", new StateConfig("Delhi", 8.00, 4.9, 54_000));
        STATE_CONFIGS.put("Uttar Pradesh", new StateConfig("Uttar Pradesh", 7.50, 5.0, 51_000));
        STATE_CONFIGS.put("Default", new StateConfig("Default", 7.50, 5.0, 52_000));
    }

    public StateConfig getStateConfig(String state) {
        if (state == null) return STATE_CONFIGS.get("Default");
        StateConfig cfg = STATE_CONFIGS.get(state);
        return cfg != null ? cfg : STATE_CONFIGS.get("Default");
    }

    public Map<String, StateConfig> getAllStateConfigs() {
        return STATE_CONFIGS;
    }

    public AreaProfile getAreaProfile(String areaType) {
        if (areaType == null) return AREA_PROFILES.get("urban");
        AreaProfile profile = AREA_PROFILES.get(areaType);
        return profile != null ? profile : AREA_PROFILES.get("urban");
    }

    public PanelRecommendation buildPanelRecommendation(double idealSystemSizeKw, String areaType) {
        AreaProfile profile = getAreaProfile(areaType);
        double capped = Math.min(idealSystemSizeKw, profile.getMaxSystemKw());
        double recommendedKw = roundToHalfKw(Math.max(1.0, capped));
        boolean cappedForArea = idealSystemSizeKw > profile.getMaxSystemKw() + 0.01;

        int panelWattage = profile.getTypicalPanelWattage();
        int panelCount = (int) Math.ceil(recommendedKw * 1000.0 / panelWattage);
        double totalCapacity = BigDecimal.valueOf(panelCount * panelWattage / 1000.0)
                .setScale(1, RoundingMode.HALF_UP).doubleValue();

        String summary = buildPanelSummary(profile, idealSystemSizeKw, recommendedKw, panelCount, panelWattage, cappedForArea);

        return PanelRecommendation.builder()
                .areaType(profile.getType())
                .areaLabel(profile.getLabel())
                .idealSystemSizeKw(roundToHalfKw(idealSystemSizeKw))
                .recommendedSystemSizeKw(recommendedKw)
                .panelCount(panelCount)
                .panelWattage(panelWattage)
                .totalPanelCapacityKw(totalCapacity)
                .summary(summary)
                .sizeCappedForArea(cappedForArea)
                .build();
    }

    private String buildPanelSummary(AreaProfile profile, double idealKw, double recommendedKw,
                                     int panelCount, int panelWattage, boolean capped) {
        if (capped) {
            return String.format(
                    "For %s areas we recommend systems up to %.0f kW. Your usage suggests %.1f kW, "
                            + "so we sized a %.1f kW array with %d × %dW panels (%.1f kW installed capacity).",
                    profile.getLabel(), profile.getMaxSystemKw(), idealKw, recommendedKw,
                    panelCount, panelWattage, panelCount * panelWattage / 1000.0
            );
        }
        return String.format(
                "Ideal for your %s location: a %.1f kW system with %d × %dW tier-1 panels "
                        + "(%.1f kW total capacity) — optimized for local grid tariffs and roof norms.",
                profile.getLabel(), recommendedKw, panelCount, panelWattage,
                panelCount * panelWattage / 1000.0
        );
    }

    private double roundToHalfKw(double kw) {
        return Math.max(1.0, Math.round(kw * 2.0) / 2.0);
    }

    public double getEffectiveTariff(double baseTariff, String propertyType) {
        if ("commercial".equalsIgnoreCase(propertyType)) {
            return baseTariff * COMMERCIAL_TARIFF_MULTIPLIER;
        }
        if ("industrial".equalsIgnoreCase(propertyType)) {
            return baseTariff * INDUSTRIAL_TARIFF_MULTIPLIER;
        }
        return baseTariff;
    }

    public double inferMonthlyUnits(double monthlyBill, double tariff) {
        if (tariff <= 0) return 0;
        return monthlyBill / tariff;
    }

    public double calculateSystemSize(double monthlyUnits, double psh) {
        if (psh <= 0) return 1.0;
        double dailyUnits = monthlyUnits / DAYS_PER_MONTH;
        double rawSize = dailyUnits / (psh * EFFICIENCY);
        double rounded = Math.round(rawSize * 2.0) / 2.0;
        return Math.max(1.0, rounded);
    }

    public long calculateSubsidy(double systemSizeKw, double costPerKw, String propertyType) {
        if (!"residential".equalsIgnoreCase(propertyType)) {
            return 0L;
        }
        double subsidy;
        if (systemSizeKw <= 2.0) {
            subsidy = systemSizeKw * costPerKw * 0.30;
        } else if (systemSizeKw <= 3.0) {
            subsidy = 2.0 * costPerKw * 0.30 + (systemSizeKw - 2.0) * costPerKw * 0.20;
        } else {
            // capped at the 3 kW amount
            subsidy = 2.0 * costPerKw * 0.30 + 1.0 * costPerKw * 0.20;
        }
        return Math.round(subsidy);
    }

    public List<ProjectionYear> buildProjection(long installCost, long annualSavings, long subsidyAmount) {
        List<ProjectionYear> projection = new ArrayList<>(25);
        long netCost = installCost - subsidyAmount;
        long cumulative = 0L;
        for (int year = 1; year <= 25; year++) {
            double escalation = Math.pow(1.0 + TARIFF_ESCALATION_PCT, year - 1);
            double degradation = Math.pow(1.0 - DEGRADATION_PCT, year - 1);
            long yearSavings = Math.round(annualSavings * escalation * degradation);
            cumulative += yearSavings;
            long netPosition = cumulative - netCost;
            projection.add(new ProjectionYear(year, yearSavings, cumulative, netPosition));
        }
        return projection;
    }

    public AssessmentResultData runFullAssessment(AssessmentRequestDTO req) {
        StateConfig cfg = getStateConfig(req.getState());
        double effectiveTariff = getEffectiveTariff(cfg.getTariff(), req.getPropertyType());

        double monthlyBill = req.getMonthlyBill().doubleValue();
        double monthlyUnits;
        if (req.getMonthlyUnits() != null && req.getMonthlyUnits().doubleValue() > 0) {
            monthlyUnits = req.getMonthlyUnits().doubleValue();
        } else {
            monthlyUnits = inferMonthlyUnits(monthlyBill, effectiveTariff);
        }

        double idealSystemSizeKw = calculateSystemSize(monthlyUnits, cfg.getPsh());
        PanelRecommendation panelRec = buildPanelRecommendation(idealSystemSizeKw, req.getAreaType());
        double systemSizeKw = panelRec.getRecommendedSystemSizeKw();
        long installCost = Math.round(systemSizeKw * cfg.getCpkw());
        long subsidyAmount = calculateSubsidy(systemSizeKw, cfg.getCpkw(), req.getPropertyType());
        long netCost = installCost - subsidyAmount;

        double monthlyGenerationKwh = systemSizeKw * cfg.getPsh() * DAYS_PER_MONTH * EFFICIENCY;
        double rawMonthlySavings = monthlyGenerationKwh * effectiveTariff;
        long monthlySavings = Math.round(Math.min(rawMonthlySavings, monthlyBill));
        long annualSavings = monthlySavings * 12;
        double paybackYears;
        if (annualSavings == 0) {
            paybackYears = 99;
        } else {
            paybackYears = (double) netCost / (double) annualSavings;
            paybackYears = BigDecimal.valueOf(paybackYears).setScale(1, RoundingMode.HALF_UP).doubleValue();
        }
        double annualGenerationKwh = monthlyGenerationKwh * 12;
        long co2OffsetKg = Math.round(annualGenerationKwh * CO2_PER_KWH);

        int roofAreaRequired = (int) Math.round(systemSizeKw * ROOF_SQFT_PER_KW);
        boolean roofAreaSufficient = true;
        if (req.getRoofAreaSqft() != null && req.getRoofAreaSqft().doubleValue() > 0) {
            roofAreaSufficient = req.getRoofAreaSqft().doubleValue() >= roofAreaRequired;
        }

        List<ProjectionYear> projection = buildProjection(installCost, annualSavings, subsidyAmount);

        double sizeRounded = BigDecimal.valueOf(systemSizeKw).setScale(1, RoundingMode.HALF_UP).doubleValue();

        return AssessmentResultData.builder()
                .systemSizeKw(sizeRounded)
                .idealSystemSizeKw(panelRec.getIdealSystemSizeKw())
                .panelRecommendation(panelRec)
                .installCost(installCost)
                .monthlySavings(monthlySavings)
                .annualSavings(annualSavings)
                .paybackYears(paybackYears)
                .co2OffsetKg(co2OffsetKg)
                .subsidyAmount(subsidyAmount)
                .netCost(netCost)
                .roofAreaRequiredSqft(roofAreaRequired)
                .roofAreaSufficient(roofAreaSufficient)
                .projection25yr(projection)
                .build();
    }

    public int calculateLeadScore(AssessmentRequestDTO req, AssessmentResultData result) {
        int score = 0;

        double bill = req.getMonthlyBill().doubleValue();
        if (bill >= 8000) score += 40;
        else if (bill >= 5000) score += 32;
        else if (bill >= 3000) score += 22;
        else if (bill >= 1500) score += 12;
        else score += 5;

        if ("own".equalsIgnoreCase(req.getOwnership())) score += 20;
        else score += 3;

        if (req.getRoofAreaSqft() == null || req.getRoofAreaSqft().doubleValue() <= 0) {
            score += 15;
        } else {
            double area = req.getRoofAreaSqft().doubleValue();
            double required = result.getRoofAreaRequiredSqft();
            if (area >= required) score += 20;
            else if (area >= 0.70 * required) score += 10;
            else score += 4;
        }

        StateConfig cfg = getStateConfig(req.getState());
        double psh = cfg.getPsh();
        if (psh >= 5.5) score += 20;
        else if (psh >= 5.0) score += 15;
        else if (psh >= 4.5) score += 10;
        else score += 5;

        return Math.min(100, score);
    }
}
