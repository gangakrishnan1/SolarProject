package com.lumenor.solariq.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContactCaptureDTO {

    @Size(max = 100)
    private String name;

    @Email(message = "email must be a valid email format")
    @Size(max = 150)
    private String email;

    @Size(max = 15)
    private String phone;

    public boolean hasAtLeastOneContact() {
        return (email != null && !email.isBlank()) || (phone != null && !phone.isBlank());
    }
}
