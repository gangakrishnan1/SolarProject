package com.lumenor.solariq.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LocationResultDTO {
    private String city;
    private String state;
    private String displayName;
    private double latitude;
    private double longitude;
    private String suggestedAreaType;
}
