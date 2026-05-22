package com.lumenor.solariq.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectionYear {
    @JsonProperty("year")
    private int year;

    @JsonProperty("annualSavings")
    private long annualSavings;

    @JsonProperty("cumulativeSavings")
    private long cumulativeSavings;

    @JsonProperty("netPosition")
    private long netPosition;
}
