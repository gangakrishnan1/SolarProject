package com.lumenor.solariq.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PanelRecommendationTest {

    private final CalculatorService svc = new CalculatorService();

    @Test
    void semiUrban_capsSystemAt8Kw() {
        PanelRecommendation rec = svc.buildPanelRecommendation(12.0, "semi_urban");
        assertEquals(8.0, rec.getRecommendedSystemSizeKw(), 0.01);
        assertTrue(rec.isSizeCappedForArea());
        assertEquals("semi_urban", rec.getAreaType());
    }

    @Test
    void rural_capsSystemAt5Kw() {
        PanelRecommendation rec = svc.buildPanelRecommendation(10.0, "rural");
        assertEquals(5.0, rec.getRecommendedSystemSizeKw(), 0.01);
        assertTrue(rec.isSizeCappedForArea());
    }

    @Test
    void urban_doesNotCapSmallSystem() {
        PanelRecommendation rec = svc.buildPanelRecommendation(4.0, "urban");
        assertEquals(4.0, rec.getRecommendedSystemSizeKw(), 0.01);
        assertFalse(rec.isSizeCappedForArea());
        assertTrue(rec.getPanelCount() > 0);
    }
}
