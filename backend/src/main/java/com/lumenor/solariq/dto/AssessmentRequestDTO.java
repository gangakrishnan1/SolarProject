package com.lumenor.solariq.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentRequestDTO {

    @NotBlank(message = "city is required")
    @Size(max = 100, message = "city must be at most 100 characters")
    private String city;

    @NotBlank(message = "state is required")
    @Size(max = 100, message = "state must be at most 100 characters")
    private String state;

    @NotBlank(message = "propertyType is required")
    @Pattern(regexp = "residential|commercial|industrial",
            message = "propertyType must be one of residential, commercial, industrial")
    private String propertyType;

    @NotNull(message = "monthlyBill is required")
    @DecimalMin(value = "0.01", inclusive = true, message = "monthlyBill must be greater than zero")
    @DecimalMax(value = "500000", inclusive = true, message = "monthlyBill must not exceed 500000")
    private BigDecimal monthlyBill;

    @DecimalMin(value = "0.01", inclusive = true, message = "monthlyUnits must be positive when provided")
    private BigDecimal monthlyUnits;

    @DecimalMin(value = "0.0", inclusive = true, message = "roofAreaSqft must be zero or positive")
    private BigDecimal roofAreaSqft;

    @Pattern(regexp = "flat_rcc|sloped", message = "roofType must be one of flat_rcc, sloped")
    private String roofType = "flat_rcc";

    @Pattern(regexp = "own|rented", message = "ownership must be one of own, rented")
    private String ownership = "own";

    @Pattern(regexp = "rural|semi_urban|urban|metro",
            message = "areaType must be one of rural, semi_urban, urban, metro")
    private String areaType = "urban";

    public String getAreaType() {
        return areaType == null ? "urban" : areaType;
    }

    public String getRoofType() {
        return roofType == null ? "flat_rcc" : roofType;
    }

    public String getOwnership() {
        return ownership == null ? "own" : ownership;
    }
}
