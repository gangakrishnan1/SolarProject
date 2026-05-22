package com.lumenor.solariq.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lumenor.solariq.dto.LocationResultDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class LocationServiceTest {

    private LocationService locationService;
    private MockRestServiceServer server;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws Exception {
        locationService = new LocationService();
        RestTemplate restTemplate = new RestTemplate();
        var field = LocationService.class.getDeclaredField("restTemplate");
        field.setAccessible(true);
        field.set(locationService, restTemplate);
        server = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    void reverse_resolvesDelhiWhenStateFieldMissing() throws Exception {
        String body = """
                {
                  "lat": "28.6139",
                  "lon": "77.2090",
                  "display_name": "Connaught Place, New Delhi, India",
                  "address": {
                    "city": "New Delhi",
                    "suburb": "Connaught Place",
                    "country": "India",
                    "country_code": "in",
                    "ISO3166-2-lvl4": "IN-DL"
                  }
                }
                """;
        server.expect(requestTo(org.hamcrest.Matchers.containsString("/reverse")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        LocationResultDTO result = locationService.reverse(28.6139, 77.2090);

        assertEquals("New Delhi", result.getCity());
        assertEquals("Delhi", result.getState());
        assertEquals("Connaught Place, New Delhi, India", result.getDisplayName());
        assertEquals("metro", result.getSuggestedAreaType());
        server.verify();
    }

    @Test
    void reverse_rejectsNonIndiaCoordinates() throws Exception {
        String body = """
                {
                  "lat": "51.5074",
                  "lon": "-0.1278",
                  "address": {
                    "city": "London",
                    "country": "United Kingdom",
                    "country_code": "gb"
                  }
                }
                """;
        server.expect(requestTo(org.hamcrest.Matchers.containsString("/reverse")))
                .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> locationService.reverse(51.5074, -0.1278)
        );
        assertEquals("Your coordinates are outside India. Please search for your Indian city instead.", ex.getMessage());
        server.verify();
    }
}
