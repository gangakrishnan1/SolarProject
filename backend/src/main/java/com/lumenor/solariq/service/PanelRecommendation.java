package com.lumenor.solariq.service;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PanelRecommendation {
    private String areaType;
    private String areaLabel;
    private double idealSystemSizeKw;
    private double recommendedSystemSizeKw;
    private int panelCount;
    private int panelWattage;
    private double totalPanelCapacityKw;
    private String summary;
    private boolean sizeCappedForArea;
}
