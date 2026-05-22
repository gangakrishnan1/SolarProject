package com.lumenor.solariq.controller;

import com.lumenor.solariq.dto.LocationResultDTO;
import com.lumenor.solariq.service.LocationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/location")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping("/search")
    public ResponseEntity<List<LocationResultDTO>> search(@RequestParam String q) {
        return ResponseEntity.ok(locationService.search(q));
    }

    @GetMapping("/reverse")
    public ResponseEntity<LocationResultDTO> reverse(
            @RequestParam double lat,
            @RequestParam double lon) {
        return ResponseEntity.ok(locationService.reverse(lat, lon));
    }
}
