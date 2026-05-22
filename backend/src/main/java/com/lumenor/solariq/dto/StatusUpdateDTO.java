package com.lumenor.solariq.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StatusUpdateDTO {
    @NotBlank(message = "status is required")
    private String status;

    private String notes;
    private String assignedTo;
}
