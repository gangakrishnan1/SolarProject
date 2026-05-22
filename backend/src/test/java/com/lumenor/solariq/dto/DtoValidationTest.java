package com.lumenor.solariq.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DtoValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setup() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void teardown() {
        factory.close();
    }

    private AssessmentRequestDTO valid() {
        AssessmentRequestDTO r = new AssessmentRequestDTO();
        r.setCity("Hyderabad");
        r.setState("Telangana");
        r.setPropertyType("residential");
        r.setMonthlyBill(new BigDecimal("4500"));
        r.setRoofType("flat_rcc");
        r.setOwnership("own");
        r.setAreaType("urban");
        return r;
    }

    @Test
    void missingCity_failsValidation() {
        AssessmentRequestDTO r = valid();
        r.setCity(null);
        Set<ConstraintViolation<AssessmentRequestDTO>> v = validator.validate(r);
        assertTrue(v.stream().anyMatch(e -> e.getPropertyPath().toString().equals("city")));
    }

    @Test
    void negativeMonthlyBill_failsValidation() {
        AssessmentRequestDTO r = valid();
        r.setMonthlyBill(new BigDecimal("-100"));
        Set<ConstraintViolation<AssessmentRequestDTO>> v = validator.validate(r);
        assertTrue(v.stream().anyMatch(e -> e.getPropertyPath().toString().equals("monthlyBill")));
    }

    @Test
    void invalidPropertyType_failsValidation() {
        AssessmentRequestDTO r = valid();
        r.setPropertyType("spaceship");
        Set<ConstraintViolation<AssessmentRequestDTO>> v = validator.validate(r);
        assertTrue(v.stream().anyMatch(e -> e.getPropertyPath().toString().equals("propertyType")));
    }

    @Test
    void validRequest_hasNoErrors() {
        Set<ConstraintViolation<AssessmentRequestDTO>> v = validator.validate(valid());
        assertTrue(v.isEmpty(), "Expected no violations but got: " + v);
    }

    @Test
    void contactCapture_hasAtLeastOneContact_email() {
        ContactCaptureDTO c = new ContactCaptureDTO();
        c.setEmail("a@b.com");
        assertTrue(c.hasAtLeastOneContact());
    }

    @Test
    void contactCapture_hasAtLeastOneContact_phone() {
        ContactCaptureDTO c = new ContactCaptureDTO();
        c.setPhone("9876543210");
        assertTrue(c.hasAtLeastOneContact());
    }

    @Test
    void contactCapture_neither_fails() {
        ContactCaptureDTO c = new ContactCaptureDTO();
        assertFalse(c.hasAtLeastOneContact());
    }
}
