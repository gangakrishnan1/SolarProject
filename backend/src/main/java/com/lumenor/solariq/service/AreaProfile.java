package com.lumenor.solariq.service;

import lombok.Getter;

@Getter
public class AreaProfile {
    private final String type;
    private final String label;
    private final double maxSystemKw;
    private final int typicalPanelWattage;

    public AreaProfile(String type, String label, double maxSystemKw, int typicalPanelWattage) {
        this.type = type;
        this.label = label;
        this.maxSystemKw = maxSystemKw;
        this.typicalPanelWattage = typicalPanelWattage;
    }
}
