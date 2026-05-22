package com.lumenor.solariq.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lumenor.solariq.dto.AssessmentRequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class AiService {

    private static final Logger log = LoggerFactory.getLogger(AiService.class);
    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s";

    private final String apiKey;
    private final String model;
    private final ObjectMapper mapper = new ObjectMapper();
    private final RestTemplate restTemplate;

    public AiService(
            @Value("${gemini.api.key:}") String apiKey,
            @Value("${gemini.api.model:gemini-1.5-flash}") String model
    ) {
        this.apiKey = apiKey;
        this.model = model;
        this.restTemplate = new RestTemplate();
    }

    public AiAnalysisResult getAiAnalysis(AssessmentRequestDTO req, AssessmentResultData result) {
        try {
            if (apiKey == null || apiKey.isBlank()) {
                log.info("Gemini API key not configured, using fallback");
                return fallback(req, result);
            }
            String prompt = buildPrompt(req, result);
            String response = callGemini(prompt);
            return parseResponse(response, req, result);
        } catch (Exception e) {
            log.warn("AI analysis failed, using fallback: {}", e.getMessage());
            return fallback(req, result);
        }
    }

    private String buildPrompt(AssessmentRequestDTO req, AssessmentResultData result) {
        StringBuilder sb = new StringBuilder();
        sb.append("Customer location: ").append(req.getCity()).append(", ").append(req.getState()).append("\n");
        sb.append("Monthly electricity bill: Rs ").append(req.getMonthlyBill()).append("\n");
        sb.append("Property type: ").append(req.getPropertyType()).append("\n");
        sb.append("Ownership: ").append(req.getOwnership()).append("\n");
        sb.append("Roof type: ").append(req.getRoofType()).append("\n");
        if (req.getRoofAreaSqft() != null) {
            sb.append("Roof area available: ").append(req.getRoofAreaSqft()).append(" sq ft\n");
        }
        sb.append("Area type: ").append(req.getAreaType()).append("\n");
        sb.append("Ideal system size from usage: ").append(result.getIdealSystemSizeKw()).append(" kW\n");
        sb.append("Recommended system size: ").append(result.getSystemSizeKw()).append(" kW\n");
        if (result.getPanelRecommendation() != null) {
            sb.append("Panel recommendation: ").append(result.getPanelRecommendation().getSummary()).append("\n");
        }
        sb.append("Installation cost: Rs ").append(result.getInstallCost()).append("\n");
        sb.append("Monthly savings: Rs ").append(result.getMonthlySavings()).append("\n");
        sb.append("Payback period: ").append(result.getPaybackYears()).append(" years\n");
        sb.append("MNRE subsidy: Rs ").append(result.getSubsidyAmount()).append("\n\n");
        sb.append("Return ONLY a raw JSON object with no markdown, no code blocks, no extra text. ");
        sb.append("The JSON must have exactly two keys: \"summary\" and \"tips\". ");
        sb.append("The \"summary\" value is a warm, friendly 2-3 sentence paragraph in simple everyday language ");
        sb.append("mentioning the city, monthly savings in rupees, and payback years. ");
        sb.append("The \"tips\" value is a list of exactly 3 strings. ");
        sb.append("Each tip must be specific to this customer's location, property type, roof type, and ownership. ");
        sb.append("Do not give generic advice.");
        return sb.toString();
    }

    private String callGemini(String prompt) {
        String url = String.format(GEMINI_URL, model, apiKey);
        ObjectNode body = mapper.createObjectNode();
        ArrayNode contents = body.putArray("contents");
        ObjectNode content = contents.addObject();
        ArrayNode parts = content.putArray("parts");
        parts.addObject().put("text", prompt);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> req = new HttpEntity<>(body.toString(), headers);

        RestTemplate rt = new RestTemplate();
        rt.getRequestFactory();
        ResponseEntity<String> resp = rt.postForEntity(url, req, String.class);
        return resp.getBody();
    }

    private AiAnalysisResult parseResponse(String raw, AssessmentRequestDTO req, AssessmentResultData result) throws Exception {
        JsonNode root = mapper.readTree(raw);
        JsonNode candidates = root.path("candidates");
        if (!candidates.isArray() || candidates.size() == 0) {
            throw new IllegalStateException("No candidates returned");
        }
        String text = candidates.get(0).path("content").path("parts").get(0).path("text").asText();
        if (text == null || text.isBlank()) {
            throw new IllegalStateException("Empty text from Gemini");
        }
        String cleaned = text.trim();
        if (cleaned.startsWith("```")) {
            int firstNewline = cleaned.indexOf('\n');
            if (firstNewline > 0) cleaned = cleaned.substring(firstNewline + 1);
            if (cleaned.endsWith("```")) cleaned = cleaned.substring(0, cleaned.length() - 3);
            cleaned = cleaned.trim();
        }
        JsonNode parsed = mapper.readTree(cleaned);
        String summary = parsed.path("summary").asText();
        JsonNode tipsNode = parsed.path("tips");
        if (summary == null || summary.isBlank() || !tipsNode.isArray() || tipsNode.size() != 3) {
            throw new IllegalStateException("Missing or malformed summary/tips");
        }
        List<String> tips = new ArrayList<>(3);
        for (JsonNode t : tipsNode) tips.add(t.asText());
        return new AiAnalysisResult(summary, tips);
    }

    private AiAnalysisResult fallback(AssessmentRequestDTO req, AssessmentResultData result) {
        String city = safe(req.getCity(), "your city");
        String state = safe(req.getState(), "your state");
        String roofType = "flat_rcc".equalsIgnoreCase(req.getRoofType()) ? "flat RCC" : "sloped";
        String propertyType = safe(req.getPropertyType(), "residential");

        String summary = String.format(
                "Going solar in %s is a smart move — you could save around Rs %d every month on your electricity bills, "
                        + "and the system pays for itself in about %.1f years. After that the savings keep flowing for another two decades, "
                        + "all while cutting your carbon footprint.",
                city, result.getMonthlySavings(), result.getPaybackYears()
        );

        List<String> tips = new ArrayList<>(3);
        String areaLabel = formatAreaLabel(req.getAreaType());
        if (result.getPanelRecommendation() != null) {
            PanelRecommendation pr = result.getPanelRecommendation();
            tips.add(String.format(
                    "%s — %s",
                    areaLabel, pr.getSummary()
            ));
        } else {
            tips.add(String.format(
                    "Your %s roof is well-suited for a %.1f kW system — tilt panels 15-20° south for peak generation.",
                    roofType, result.getSystemSizeKw()
            ));
        }
        tips.add(String.format(
                "In %s, sign up for net metering with your local DISCOM right after installation so any extra power you generate is credited back against your future bills.",
                state
        ));
        if ("residential".equalsIgnoreCase(propertyType) && result.getSubsidyAmount() > 0) {
            tips.add(String.format(
                    "You qualify for the MNRE PM Surya Ghar subsidy of about Rs %d — apply through the national portal before you finalize the installation to make sure you capture it.",
                    result.getSubsidyAmount()
            ));
        } else {
            tips.add("As a commercial installation you can claim 40 percent accelerated depreciation on the system cost in the first year — work with your CA to factor this into the tax planning.");
        }
        return new AiAnalysisResult(summary, tips);
    }

    private String safe(String s, String fallback) {
        return (s == null || s.isBlank()) ? fallback : s;
    }

    private String formatAreaLabel(String areaType) {
        if (areaType == null) return "Urban area";
        return switch (areaType) {
            case "rural" -> "Rural area";
            case "semi_urban" -> "Semi-urban area";
            case "metro" -> "Metro city";
            default -> "Urban area";
        };
    }
}
