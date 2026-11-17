package com.htut.travelmate.controllers;

import com.htut.travelmate.common.security.UserPrincipal;
import com.htut.travelmate.dtos.CreateLocationRequestDto;
import com.htut.travelmate.dtos.LocationResponseDto;
import com.htut.travelmate.dtos.LocationStatsDto;
import com.htut.travelmate.dtos.UpdateLocationRequestDto;
import com.htut.travelmate.services.LocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller exposing endpoints for travel location management.
 * All operations are scoped to the authenticated user.
 */
@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @GetMapping
    public ResponseEntity<List<LocationResponseDto>> getUserLocations(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(locationService.getUserLocations(principal.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LocationResponseDto> getLocationById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(locationService.getLocationById(id, principal.getId()));
    }

    @GetMapping("/stats")
    public ResponseEntity<LocationStatsDto> getUserStats(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(locationService.getUserStats(principal.getId()));
    }

    @PostMapping
    public ResponseEntity<LocationResponseDto> createLocation(
            @Valid @RequestBody CreateLocationRequestDto requestDto,
            @AuthenticationPrincipal UserPrincipal principal) {
        LocationResponseDto created = locationService.createLocation(requestDto, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LocationResponseDto> updateLocation(
            @PathVariable Long id,
            @Valid @RequestBody UpdateLocationRequestDto requestDto,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(locationService.updateLocation(id, requestDto, principal.getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLocation(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        locationService.deleteLocation(id, principal.getId());
        return ResponseEntity.noContent().build();
    }
}
