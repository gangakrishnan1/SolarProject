package com.lumenor.solariq.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectionYearDTO {
    private int year;
    private long annualSavings;
    private long cumulativeSavings;
    private long netPosition;
}
