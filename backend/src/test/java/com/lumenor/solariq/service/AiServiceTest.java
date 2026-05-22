package com.lumenor.solariq.service;

import com.lumenor.solariq.dto.AssessmentRequestDTO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class AiServiceTest {

    private AssessmentRequestDTO sampleReq() {
        AssessmentRequestDTO req = new AssessmentRequestDTO();
        req.setCity("Hyderabad");
        req.setState("Telangana");
        req.setPropertyType("residential");
        req.setMonthlyBill(new BigDecimal("4500"));
        req.setRoofType("flat_rcc");
        req.setOwnership("own");
        req.setRoofAreaSqft(new BigDecimal("500"));
        return req;
    }

    private AssessmentResultData sampleResult() {
        return AssessmentResultData.builder()
                .systemSizeKw(2.5)
                .installCost(125_000)
                .monthlySavings(3200)
                .annualSavings(38_400)
                .paybackYears(2.4)
                .co2OffsetKg(2600)
                .subsidyAmount(85_000)
                .netCost(40_000)
                .roofAreaRequiredSqft(250)
                .roofAreaSufficient(true)
                .projection25yr(Collections.emptyList())
                .build();
    }

    @Test
    void fallback_returnsValidData_whenApiKeyEmpty() {
        AiService svc = new AiService("", "gemini-1.5-flash");
        AiAnalysisResult r = svc.getAiAnalysis(sampleReq(), sampleResult());
        assertNotNull(r);
        assertNotNull(r.getSummary());
        assertFalse(r.getSummary().isBlank());
        assertTrue(r.getSummary().length() > 50,
                "Summary length " + r.getSummary().length() + " too short");
        assertNotNull(r.getTips());
        assertEquals(3, r.getTips().size());
        for (String tip : r.getTips()) {
            assertNotNull(tip);
            assertFalse(tip.isBlank());
            assertTrue(tip.length() > 10, "Tip too short: " + tip);
        }
    }

    @Test
    void fallback_neverThrows_evenWithNullFields() {
        AssessmentRequestDTO req = new AssessmentRequestDTO();
        req.setCity("X");
        req.setState("Y");
        req.setPropertyType("commercial");
        req.setMonthlyBill(new BigDecimal("10000"));
        AiService svc = new AiService("", "gemini-1.5-flash");
        assertDoesNotThrow(() -> svc.getAiAnalysis(req, sampleResult()));
    }

    @Test
    void fallback_mentionsCity() {
        AiService svc = new AiService("", "gemini-1.5-flash");
        AiAnalysisResult r = svc.getAiAnalysis(sampleReq(), sampleResult());
        assertTrue(r.getSummary().contains("Hyderabad"), "Summary should mention city");
    }

    @Test
    void fallback_commercialTipMentionsTaxBenefit() {
        AssessmentRequestDTO req = sampleReq();
        req.setPropertyType("commercial");
        AiService svc = new AiService("", "gemini-1.5-flash");
        AiAnalysisResult r = svc.getAiAnalysis(req, sampleResult());
        String allTips = String.join(" ", r.getTips()).toLowerCase();
        assertTrue(allTips.contains("depreciation") || allTips.contains("tax"),
                "Commercial tips should mention tax benefit");
    }
}
