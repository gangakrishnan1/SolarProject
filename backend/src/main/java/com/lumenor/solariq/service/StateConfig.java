package com.lumenor.solariq.service;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StateConfig {
    private final String name;
    private final double tariff;
    private final double psh;
    private final double cpkw;
}
