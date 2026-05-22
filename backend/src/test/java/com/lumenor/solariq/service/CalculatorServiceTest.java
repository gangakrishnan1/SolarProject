package com.lumenor.solariq.service;

import com.lumenor.solariq.dto.AssessmentRequestDTO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CalculatorServiceTest {

    private final CalculatorService svc = new CalculatorService();

    @Test
    void getStateConfig_telangana_tariff_750() {
        StateConfig cfg = svc.getStateConfig("Telangana");
        assertEquals(7.50, cfg.getTariff(), 0.001);
    }

    @Test
    void getStateConfig_unknown_returnsDefault() {
        StateConfig cfg = svc.getStateConfig("MarsColony");
        assertNotNull(cfg);
        assertEquals("Default", cfg.getName());
    }

    @Test
    void getStateConfig_allTenStatesHaveValues() {
        String[] states = {"Telangana", "Andhra Pradesh", "Karnataka", "Tamil Nadu",
                "Kerala", "Maharashtra", "Gujarat", "Rajasthan", "Delhi", "Uttar Pradesh"};
        for (String s : states) {
            StateConfig cfg = svc.getStateConfig(s);
            assertTrue(cfg.getTariff() > 0, s + " tariff");
            assertTrue(cfg.getPsh() > 0, s + " psh");
            assertTrue(cfg.getCpkw() > 0, s + " cpkw");
        }
    }

    @Test
    void inferMonthlyUnits_4500at750_returns600() {
        double units = svc.inferMonthlyUnits(4500, 7.50);
        assertEquals(600.0, units, 0.01);
    }

    @Test
    void calculateSystemSize_returnsPositive() {
        double size = svc.calculateSystemSize(600, 5.2);
        assertTrue(size > 0);
    }

    @Test
    void calculateSystemSize_isMultipleOfHalf() {
        for (double units : new double[]{100, 300, 600, 900, 1500, 2400}) {
            double size = svc.calculateSystemSize(units, 5.2);
            double doubled = size * 2.0;
            assertEquals(Math.round(doubled), doubled, 0.0001,
                    "size " + size + " for units " + units + " is not a 0.5 multiple");
        }
    }

    @Test
    void calculateSystemSize_minimumOneKw() {
        double size = svc.calculateSystemSize(5, 5.2);
        assertTrue(size >= 1.0, "Expected >=1.0 but got " + size);
    }

    @Test
    void calculateSystemSize_largerUnitsGivesLargerSize() {
        double smaller = svc.calculateSystemSize(300, 5.2);
        double larger = svc.calculateSystemSize(900, 5.2);
        assertTrue(larger > smaller);
    }

    @Test
    void calculateSubsidy_residential_15kw_50000() {
        long subsidy = svc.calculateSubsidy(1.5, 50_000, "residential");
        long expected = Math.round(1.5 * 50_000 * 0.30);
        assertEquals(expected, subsidy);
    }

    @Test
    void calculateSubsidy_residential_25kw_usesSplitFormula() {
        long subsidy = svc.calculateSubsidy(2.5, 50_000, "residential");
        long expected = Math.round(2.0 * 50_000 * 0.30 + 0.5 * 50_000 * 0.20);
        assertEquals(expected, subsidy);
    }

    @Test
    void calculateSubsidy_residential_5kw_cappedAt3kw() {
        long cap = svc.calculateSubsidy(3.0, 50_000, "residential");
        long fiveKw = svc.calculateSubsidy(5.0, 50_000, "residential");
        assertEquals(cap, fiveKw);
    }

    @Test
    void calculateSubsidy_commercial_zero() {
        assertEquals(0L, svc.calculateSubsidy(5.0, 50_000, "commercial"));
    }

    @Test
    void calculateSubsidy_industrial_zero() {
        assertEquals(0L, svc.calculateSubsidy(5.0, 50_000, "industrial"));
    }

    @Test
    void buildProjection_returns25Entries() {
        List<ProjectionYear> p = svc.buildProjection(150_000, 30_000, 40_000);
        assertEquals(25, p.size());
    }

    @Test
    void buildProjection_firstEntryYearOne() {
        List<ProjectionYear> p = svc.buildProjection(150_000, 30_000, 40_000);
        assertEquals(1, p.get(0).getYear());
    }

    @Test
    void buildProjection_cumulativeIncreases() {
        List<ProjectionYear> p = svc.buildProjection(150_000, 30_000, 40_000);
        for (int i = 1; i < p.size(); i++) {
            assertTrue(p.get(i).getCumulativeSavings() > p.get(i - 1).getCumulativeSavings(),
                    "Cumulative should grow at year " + p.get(i).getYear());
        }
    }

    @Test
    void buildProjection_paybackWithin25Years() {
        List<ProjectionYear> p = svc.buildProjection(100_000, 25_000, 30_000);
        boolean found = p.stream().anyMatch(y -> y.getNetPosition() >= 0);
        assertTrue(found, "Expected break-even within 25 years for high-savings case");
    }

    private AssessmentRequestDTO sampleResidential() {
        AssessmentRequestDTO req = new AssessmentRequestDTO();
        req.setCity("Hyderabad");
        req.setState("Telangana");
        req.setPropertyType("residential");
        req.setMonthlyBill(new BigDecimal("4500"));
        req.setRoofType("flat_rcc");
        req.setOwnership("own");
        return req;
    }

    @Test
    void runFullAssessment_positiveSystemSize() {
        AssessmentResultData r = svc.runFullAssessment(sampleResidential());
        assertTrue(r.getSystemSizeKw() > 0);
    }

    @Test
    void runFullAssessment_savingsNeverExceedBill() {
        for (String s : new String[]{"500", "1500", "3000", "5000", "10000", "25000"}) {
            AssessmentRequestDTO req = sampleResidential();
            req.setMonthlyBill(new BigDecimal(s));
            AssessmentResultData r = svc.runFullAssessment(req);
            assertTrue(r.getMonthlySavings() <= Long.parseLong(s),
                    "Bill=" + s + " savings=" + r.getMonthlySavings());
        }
    }

    @Test
    void runFullAssessment_residentialPositiveSubsidy() {
        AssessmentResultData r = svc.runFullAssessment(sampleResidential());
        assertTrue(r.getSubsidyAmount() > 0);
    }

    @Test
    void runFullAssessment_commercialZeroSubsidy() {
        AssessmentRequestDTO req = sampleResidential();
        req.setPropertyType("commercial");
        AssessmentResultData r = svc.runFullAssessment(req);
        assertEquals(0L, r.getSubsidyAmount());
    }

    @Test
    void runFullAssessment_residentialNetCostLessThanInstall() {
        AssessmentResultData r = svc.runFullAssessment(sampleResidential());
        assertTrue(r.getNetCost() < r.getInstallCost());
    }

    @Test
    void runFullAssessment_projection25Entries() {
        AssessmentResultData r = svc.runFullAssessment(sampleResidential());
        assertEquals(25, r.getProjection25yr().size());
    }

    @Test
    void runFullAssessment_co2OffsetPositive() {
        AssessmentResultData r = svc.runFullAssessment(sampleResidential());
        assertTrue(r.getCo2OffsetKg() > 0);
    }

    @Test
    void calculateLeadScore_between0and100() {
        for (String bill : new String[]{"500", "1500", "3000", "5000", "10000"}) {
            for (String ownership : new String[]{"own", "rented"}) {
                AssessmentRequestDTO req = sampleResidential();
                req.setMonthlyBill(new BigDecimal(bill));
                req.setOwnership(ownership);
                AssessmentResultData r = svc.runFullAssessment(req);
                int score = svc.calculateLeadScore(req, r);
                assertTrue(score >= 0 && score <= 100,
                        "Score " + score + " for bill " + bill + " own " + ownership);
            }
        }
    }

    @Test
    void calculateLeadScore_highBillOwn_above70() {
        AssessmentRequestDTO req = sampleResidential();
        req.setMonthlyBill(new BigDecimal("9000"));
        req.setOwnership("own");
        AssessmentResultData r = svc.runFullAssessment(req);
        int score = svc.calculateLeadScore(req, r);
        assertTrue(score > 70, "Expected >70, got " + score);
    }

    @Test
    void calculateLeadScore_lowBillRented_below50() {
        AssessmentRequestDTO req = sampleResidential();
        req.setMonthlyBill(new BigDecimal("800"));
        req.setOwnership("rented");
        AssessmentResultData r = svc.runFullAssessment(req);
        int score = svc.calculateLeadScore(req, r);
        assertTrue(score < 50, "Expected <50, got " + score);
    }
}
