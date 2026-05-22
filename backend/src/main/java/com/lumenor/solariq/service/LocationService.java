package com.lumenor.solariq.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lumenor.solariq.dto.LocationResultDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class LocationService {

    private static final Logger log = LoggerFactory.getLogger(LocationService.class);
    private static final String NOMINATIM = "https://nominatim.openstreetmap.org";
    private static final String USER_AGENT = "LumenorSolarIQ/1.0 (solar-feasibility-app)";

    private static final Set<String> SUPPORTED_STATES = Set.of(
            "Telangana", "Andhra Pradesh", "Karnataka", "Tamil Nadu", "Kerala",
            "Maharashtra", "Gujarat", "Rajasthan", "Delhi", "Uttar Pradesh"
    );

    private static final Map<String, String> ISO_TO_STATE = Map.ofEntries(
            Map.entry("IN-TS", "Telangana"),
            Map.entry("IN-AP", "Andhra Pradesh"),
            Map.entry("IN-KA", "Karnataka"),
            Map.entry("IN-TN", "Tamil Nadu"),
            Map.entry("IN-KL", "Kerala"),
            Map.entry("IN-MH", "Maharashtra"),
            Map.entry("IN-GJ", "Gujarat"),
            Map.entry("IN-RJ", "Rajasthan"),
            Map.entry("IN-DL", "Delhi"),
            Map.entry("IN-UP", "Uttar Pradesh")
    );

    private static final Set<String> METRO_CITY_HINTS = Set.of(
            "hyderabad", "bengaluru", "bangalore", "mumbai", "delhi", "new delhi",
            "chennai", "kolkata", "pune", "ahmedabad"
    );

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    public List<LocationResultDTO> search(String query) {
        if (query == null || query.trim().length() < 2) {
            return List.of();
        }
        String url = UriComponentsBuilder.fromHttpUrl(NOMINATIM + "/search")
                .queryParam("q", query.trim())
                .queryParam("format", "json")
                .queryParam("addressdetails", 1)
                .queryParam("countrycodes", "in")
                .queryParam("limit", 8)
                .build()
                .toUriString();
        JsonNode results = fetchJson(url);
        List<LocationResultDTO> out = new ArrayList<>();
        if (results != null && results.isArray()) {
            for (JsonNode node : results) {
                LocationResultDTO dto = fromNominatim(node, false);
                if (dto.getCity() != null && !dto.getCity().isBlank()) {
                    out.add(dto);
                }
            }
        }
        return out;
    }

    public LocationResultDTO reverse(double lat, double lon) {
        if (lat < -90 || lat > 90 || lon < -180 || lon > 180) {
            throw new IllegalArgumentException("Invalid coordinates");
        }
        String url = UriComponentsBuilder.fromHttpUrl(NOMINATIM + "/reverse")
                .queryParam("lat", lat)
                .queryParam("lon", lon)
                .queryParam("format", "json")
                .queryParam("addressdetails", 1)
                .queryParam("zoom", 18)
                .build()
                .toUriString();
        JsonNode node = fetchJson(url);
        if (node == null || node.isMissingNode()) {
            throw new IllegalArgumentException("Could not resolve location for coordinates");
        }
        JsonNode address = node.path("address");
        String countryCode = text(address, "country_code");
        if (countryCode != null && !"in".equalsIgnoreCase(countryCode)) {
            throw new IllegalArgumentException(
                    "Your coordinates are outside India. Please search for your Indian city instead.");
        }
        LocationResultDTO dto = fromNominatim(node, false);
        if (dto.getDisplayName() == null || dto.getDisplayName().isBlank()) {
            dto.setDisplayName(buildFullAddress(node.path("address"), dto.getCity(), dto.getState()));
        }
        if (dto.getCity() == null || dto.getCity().isBlank()) {
            throw new IllegalArgumentException(
                    "Could not determine your city from GPS. Please search for your locality.");
        }
        if (dto.getState() == null || !SUPPORTED_STATES.contains(dto.getState())) {
            throw new IllegalArgumentException(
                    "We support 10 Indian states for solar estimates. Please pick your state from the dropdown.");
        }
        return dto;
    }

    private JsonNode fetchJson(String url) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", USER_AGENT);
            headers.set("Accept-Language", "en");
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            return mapper.readTree(resp.getBody());
        } catch (Exception e) {
            log.warn("Nominatim request failed: {}", e.getMessage());
            return null;
        }
    }

    private LocationResultDTO fromNominatim(JsonNode node, boolean shortDisplay) {
        JsonNode address = node.path("address");
        String city = resolveCity(address);
        String state = resolveState(address, city);
        double lat = parseCoord(node.path("lat").asText("0"));
        double lon = parseCoord(node.path("lon").asText("0"));
        String display = shortDisplay
                ? buildShortLabel(city, state)
                : node.path("display_name").asText("");
        if (display.isBlank() && city != null) {
            display = buildShortLabel(city, state);
        }
        String suggestedArea = inferAreaType(address, city, state);
        return new LocationResultDTO(city, state, display, lat, lon, suggestedArea);
    }

    private double parseCoord(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String resolveCity(JsonNode address) {
        String city = firstNonBlank(
                text(address, "city"),
                text(address, "town"),
                text(address, "municipality"),
                text(address, "village"),
                text(address, "county")
        );
        if (city != null) {
            return normalizeCityName(city);
        }

        String district = firstNonBlank(
                text(address, "state_district"),
                text(address, "district"),
                text(address, "county")
        );
        if (district != null && looksLikeCityName(district)) {
            return normalizeCityName(district);
        }

        String suburb = firstNonBlank(
                text(address, "suburb"),
                text(address, "neighbourhood"),
                text(address, "locality"),
                text(address, "city_district")
        );
        if (suburb != null) {
            String metro = metroFromContext(address, suburb);
            if (metro != null) {
                return metro;
            }
            if (!looksLikeWardOrRoad(suburb)) {
                return normalizeCityName(suburb);
            }
        }

        return district != null ? normalizeCityName(district) : null;
    }

    private String metroFromContext(JsonNode address, String suburb) {
        String stateDistrict = text(address, "state_district");
        if (stateDistrict != null && looksLikeCityName(stateDistrict)) {
            return normalizeCityName(stateDistrict);
        }
        String state = text(address, "state");
        if (state != null) {
            String lower = state.toLowerCase();
            if (lower.contains("telangana") && !suburb.toLowerCase().contains("hyderabad")) {
                return "Hyderabad";
            }
            if (lower.contains("karnataka") && !suburb.toLowerCase().contains("bengaluru")) {
                return "Bengaluru";
            }
            if (lower.contains("maharashtra") && !suburb.toLowerCase().contains("mumbai")) {
                return "Mumbai";
            }
        }
        return null;
    }

    private boolean looksLikeWardOrRoad(String value) {
        String lower = value.toLowerCase();
        return lower.contains("ward ") || lower.contains(" road") || lower.contains("ghmc")
                || lower.contains("municipal corporation");
    }

    private boolean looksLikeCityName(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        String lower = value.toLowerCase();
        if (looksLikeWardOrRoad(value)) {
            return false;
        }
        if (lower.contains("mandal") || lower.contains("district") || lower.contains("taluk")) {
            return lower.contains("hyderabad") || lower.contains("bengaluru") || lower.contains("bangalore")
                    || lower.contains("mumbai") || lower.contains("chennai") || lower.contains("delhi");
        }
        return true;
    }

    private String normalizeCityName(String city) {
        if (city == null) {
            return null;
        }
        String trimmed = city.trim();
        if (trimmed.equalsIgnoreCase("Bangalore")) {
            return "Bengaluru";
        }
        if (trimmed.equalsIgnoreCase("Bombay")) {
            return "Mumbai";
        }
        return trimmed;
    }

    private String resolveState(JsonNode address, String city) {
        String rawState = text(address, "state");
        if (rawState != null) {
            String mapped = mapState(rawState);
            if (mapped != null) {
                return mapped;
            }
        }

        String iso = text(address, "ISO3166-2-lvl4");
        if (iso != null && ISO_TO_STATE.containsKey(iso)) {
            return ISO_TO_STATE.get(iso);
        }

        if (city != null) {
            String lower = city.toLowerCase();
            if (lower.contains("delhi")) {
                return "Delhi";
            }
            if (lower.contains("hyderabad")) {
                return "Telangana";
            }
            if (lower.contains("bengaluru") || lower.contains("bangalore")) {
                return "Karnataka";
            }
            if (lower.contains("mumbai") || lower.contains("pune")) {
                return "Maharashtra";
            }
            if (lower.contains("chennai")) {
                return "Tamil Nadu";
            }
            if (lower.contains("kolkata")) {
                return "West Bengal";
            }
        }

        return null;
    }

    private String buildShortLabel(String city, String state) {
        if (city != null && state != null) {
            return city + ", " + state;
        }
        if (city != null) {
            return city;
        }
        return "";
    }

    /** Street-level label when Nominatim display_name is missing. */
    private String buildFullAddress(JsonNode address, String city, String state) {
        List<String> parts = new ArrayList<>();
        String house = text(address, "house_number");
        String road = text(address, "road");
        if (house != null && road != null) {
            parts.add(house + " " + road);
        } else if (road != null) {
            parts.add(road);
        } else if (house != null) {
            parts.add(house);
        }
        for (String key : new String[] {
                "neighbourhood", "suburb", "locality", "city_district",
                "village", "town", "municipality", "county", "state_district"
        }) {
            String v = text(address, key);
            if (v != null && !parts.contains(v)) {
                parts.add(v);
            }
        }
        if (city != null && !parts.contains(city)) {
            parts.add(city);
        }
        if (state != null && !parts.contains(state)) {
            parts.add(state);
        }
        String postcode = text(address, "postcode");
        if (postcode != null) {
            parts.add(postcode);
        }
        if (parts.isEmpty()) {
            return buildShortLabel(city, state);
        }
        return String.join(", ", parts);
    }

    private String inferAreaType(JsonNode address, String city, String state) {
        if (city != null) {
            String lower = city.toLowerCase();
            if (METRO_CITY_HINTS.contains(lower)) {
                return "metro";
            }
        }
        if (address.has("city") || address.has("state_district")) {
            if (state != null && (state.contains("Delhi") || state.contains("Maharashtra")
                    || state.contains("Karnataka") || state.contains("Telangana")
                    || state.contains("Tamil Nadu"))) {
                return "urban";
            }
            return "urban";
        }
        if (address.has("town")) {
            return "semi_urban";
        }
        if (address.has("village") || address.has("hamlet")) {
            return "rural";
        }
        return "semi_urban";
    }

    private String mapState(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String normalized = raw.trim();
        if (normalized.contains("Telangana")) return "Telangana";
        if (normalized.contains("Andhra")) return "Andhra Pradesh";
        if (normalized.contains("Karnataka")) return "Karnataka";
        if (normalized.contains("Tamil")) return "Tamil Nadu";
        if (normalized.contains("Kerala")) return "Kerala";
        if (normalized.contains("Maharashtra")) return "Maharashtra";
        if (normalized.contains("Gujarat")) return "Gujarat";
        if (normalized.contains("Rajasthan")) return "Rajasthan";
        if (normalized.contains("Delhi") || normalized.contains("NCT")) return "Delhi";
        if (normalized.contains("Uttar")) return "Uttar Pradesh";
        for (String s : SUPPORTED_STATES) {
            if (s.equalsIgnoreCase(normalized)) return s;
        }
        return null;
    }

    private String text(JsonNode address, String key) {
        if (!address.has(key)) return null;
        String v = address.get(key).asText();
        return v.isBlank() ? null : v;
    }

    private String firstNonBlank(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) return v;
        }
        return null;
    }
}
