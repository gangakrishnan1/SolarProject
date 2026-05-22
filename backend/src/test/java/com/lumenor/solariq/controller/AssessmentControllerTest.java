package com.lumenor.solariq.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lumenor.solariq.dto.AssessmentRequestDTO;
import com.lumenor.solariq.entity.Lead;
import com.lumenor.solariq.repository.LeadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
class AssessmentControllerTest {

    @Autowired private WebApplicationContext context;
    @Autowired private LeadRepository repo;
    @Autowired private ObjectMapper mapper;

    private MockMvc mvc;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        repo.deleteAll();
    }

    private AssessmentRequestDTO validReq() {
        AssessmentRequestDTO r = new AssessmentRequestDTO();
        r.setCity("Hyderabad");
        r.setState("Telangana");
        r.setPropertyType("residential");
        r.setMonthlyBill(new BigDecimal("4500"));
        r.setRoofType("flat_rcc");
        r.setOwnership("own");
        r.setRoofAreaSqft(new BigDecimal("500"));
        return r;
    }

    private MvcResult postAssess(Object body, int expectedStatus) throws Exception {
        return mvc.perform(post("/api/v1/assess")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().is(expectedStatus))
                .andReturn();
    }

    private JsonNode parse(MvcResult r) throws Exception {
        return mapper.readTree(r.getResponse().getContentAsString());
    }

    private String createOneAndReturnId() throws Exception {
        MvcResult r = postAssess(validReq(), 201);
        return parse(r).get("assessmentId").asText();
    }

    @Test
    void postAssess_validData_returns201() throws Exception {
        postAssess(validReq(), 201);
    }

    @Test
    void postAssess_responseHasAssessmentId() throws Exception {
        JsonNode body = parse(postAssess(validReq(), 201));
        assertTrue(body.has("assessmentId") && !body.get("assessmentId").isNull());
        assertFalse(body.get("assessmentId").asText().isBlank());
    }

    @Test
    void postAssess_systemSizeKwPositive() throws Exception {
        JsonNode body = parse(postAssess(validReq(), 201));
        assertTrue(body.get("systemSizeKw").asDouble() > 0);
    }

    @Test
    void postAssess_monthlySavingsGreaterThanZero() throws Exception {
        JsonNode body = parse(postAssess(validReq(), 201));
        assertTrue(body.get("monthlySavings").asLong() > 0);
    }

    @Test
    void postAssess_monthlySavingsNotExceedBill() throws Exception {
        AssessmentRequestDTO req = validReq();
        JsonNode body = parse(postAssess(req, 201));
        assertTrue(body.get("monthlySavings").asLong() <= req.getMonthlyBill().longValue());
    }

    @Test
    void postAssess_aiSummaryNonEmpty() throws Exception {
        JsonNode body = parse(postAssess(validReq(), 201));
        assertFalse(body.get("aiSummary").asText().isBlank());
    }

    @Test
    void postAssess_aiTipsHas3Items() throws Exception {
        JsonNode body = parse(postAssess(validReq(), 201));
        assertEquals(3, body.get("aiTips").size());
    }

    @Test
    void postAssess_leadScoreBetween0and100() throws Exception {
        JsonNode body = parse(postAssess(validReq(), 201));
        int score = body.get("leadScore").asInt();
        assertTrue(score >= 0 && score <= 100);
    }

    @Test
    void postAssess_projection25Items() throws Exception {
        JsonNode body = parse(postAssess(validReq(), 201));
        assertEquals(25, body.get("projection25yr").size());
    }

    @Test
    void postAssess_leadSavedToDatabase() throws Exception {
        long before = repo.count();
        postAssess(validReq(), 201);
        assertEquals(before + 1, repo.count());
    }

    @Test
    void postAssess_missingCity_returns400() throws Exception {
        AssessmentRequestDTO r = validReq();
        r.setCity(null);
        postAssess(r, 400);
    }

    @Test
    void postAssess_blankCity_returns400() throws Exception {
        AssessmentRequestDTO r = validReq();
        r.setCity("   ");
        postAssess(r, 400);
    }

    @Test
    void postAssess_negativeBill_returns400() throws Exception {
        AssessmentRequestDTO r = validReq();
        r.setMonthlyBill(new BigDecimal("-100"));
        postAssess(r, 400);
    }

    @Test
    void postAssess_zeroBill_returns400() throws Exception {
        AssessmentRequestDTO r = validReq();
        r.setMonthlyBill(new BigDecimal("0"));
        postAssess(r, 400);
    }

    @Test
    void postAssess_invalidPropertyType_returns400() throws Exception {
        AssessmentRequestDTO r = validReq();
        r.setPropertyType("spaceship");
        postAssess(r, 400);
    }

    @Test
    void postAssess_invalidRoofType_returns400() throws Exception {
        AssessmentRequestDTO r = validReq();
        r.setRoofType("thatched");
        postAssess(r, 400);
    }

    @Test
    void capture_validEmail_returns200() throws Exception {
        String id = createOneAndReturnId();
        mvc.perform(post("/api/v1/assess/" + id + "/capture")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"a@b.com\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void capture_phoneOnly_returns200() throws Exception {
        String id = createOneAndReturnId();
        mvc.perform(post("/api/v1/assess/" + id + "/capture")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"9876543210\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void capture_emailAndPhone_returns200() throws Exception {
        String id = createOneAndReturnId();
        mvc.perform(post("/api/v1/assess/" + id + "/capture")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"a@b.com\",\"phone\":\"9876543210\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void capture_neither_returns400() throws Exception {
        String id = createOneAndReturnId();
        mvc.perform(post("/api/v1/assess/" + id + "/capture")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void capture_unknownId_returns404() throws Exception {
        String fakeId = UUID.randomUUID().toString();
        mvc.perform(post("/api/v1/assess/" + fakeId + "/capture")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"a@b.com\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void capture_persistsEmailToDatabase() throws Exception {
        String id = createOneAndReturnId();
        mvc.perform(post("/api/v1/assess/" + id + "/capture")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"persisted@example.com\"}"))
                .andExpect(status().isOk());
        Lead lead = repo.findById(UUID.fromString(id)).orElseThrow();
        assertEquals("persisted@example.com", lead.getEmail());
    }

    @Test
    void getLeads_noAuth_returns401or403() throws Exception {
        MvcResult r = mvc.perform(get("/api/v1/leads")).andReturn();
        int s = r.getResponse().getStatus();
        assertTrue(s == 401 || s == 403, "Expected 401/403 got " + s);
    }

    @Test
    void getLeads_withAuth_returns200AndList() throws Exception {
        createOneAndReturnId();
        MvcResult r = mvc.perform(get("/api/v1/leads")
                        .with(SecurityMockMvcRequestPostProcessors.httpBasic("admin", "admin123")))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode body = parse(r);
        assertTrue(body.isArray());
        assertTrue(body.size() >= 1);
    }

    @Test
    void getLeads_twoLeads_returnsBoth() throws Exception {
        createOneAndReturnId();
        createOneAndReturnId();
        MvcResult r = mvc.perform(get("/api/v1/leads")
                        .with(SecurityMockMvcRequestPostProcessors.httpBasic("admin", "admin123")))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals(2, parse(r).size());
    }

    @Test
    void patchStatus_contacted_returns200() throws Exception {
        String id = createOneAndReturnId();
        MvcResult r = mvc.perform(patch("/api/v1/leads/" + id + "/status")
                        .with(SecurityMockMvcRequestPostProcessors.httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"contacted\"}"))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals("contacted", parse(r).get("status").asText());
    }

    @Test
    void patchStatus_invalidValue_returns400() throws Exception {
        String id = createOneAndReturnId();
        mvc.perform(patch("/api/v1/leads/" + id + "/status")
                        .with(SecurityMockMvcRequestPostProcessors.httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"flying\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void patchStatus_unknownLead_returns404() throws Exception {
        String fake = UUID.randomUUID().toString();
        mvc.perform(patch("/api/v1/leads/" + fake + "/status")
                        .with(SecurityMockMvcRequestPostProcessors.httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"contacted\"}"))
                .andExpect(status().isNotFound());
    }
}
