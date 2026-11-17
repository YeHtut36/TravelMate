package com.htut.travelmate.services;

import com.htut.travelmate.common.exception.ResourceNotFoundException;
import com.htut.travelmate.dtos.CreateLocationRequestDto;
import com.htut.travelmate.dtos.LocationResponseDto;
import com.htut.travelmate.dtos.LocationStatsDto;
import com.htut.travelmate.dtos.UpdateLocationRequestDto;
import com.htut.travelmate.entities.Location;
import com.htut.travelmate.entities.User;
import com.htut.travelmate.repositories.LocationRepository;
import com.htut.travelmate.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service managing user-scoped travel location operations.
 * Enforces ownership authorization: users can only view, mutate, or delete their own locations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LocationService {

    private final LocationRepository locationRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<LocationResponseDto> getUserLocations(Long userId) {
        log.debug("Fetching all locations for userId: {}", userId);
        return locationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(LocationResponseDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public LocationResponseDto getLocationById(Long id, Long userId) {
        log.debug("Fetching location with id: {} for userId: {}", id, userId);
        return locationRepository.findByIdAndUserId(id, userId)
                .map(LocationResponseDto::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with id: " + id));
    }

    @Transactional
    public LocationResponseDto createLocation(CreateLocationRequestDto dto, Long userId) {
        log.info("Creating new location '{}' in '{}, {}' for userId: {}", dto.getTitle(), dto.getCity(), dto.getCountry(), userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Location location = Location.builder()
                .title(dto.getTitle().trim())
                .city(dto.getCity().trim())
                .country(dto.getCountry().trim())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .description(dto.getDescription())
                .category(dto.getCategory())
                .visitedAt(dto.getVisitedAt())
                .user(user)
                .build();

        Location savedLocation = locationRepository.save(location);
        return LocationResponseDto.fromEntity(savedLocation);
    }

    @Transactional
    public LocationResponseDto updateLocation(Long id, UpdateLocationRequestDto dto, Long userId) {
        log.info("Updating location with id: {} for userId: {}", id, userId);
        Location location = locationRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with id: " + id));

        location.setTitle(dto.getTitle().trim());
        location.setCity(dto.getCity().trim());
        location.setCountry(dto.getCountry().trim());
        location.setLatitude(dto.getLatitude());
        location.setLongitude(dto.getLongitude());
        location.setDescription(dto.getDescription());
        location.setCategory(dto.getCategory());
        location.setVisitedAt(dto.getVisitedAt());

        Location updatedLocation = locationRepository.save(location);
        return LocationResponseDto.fromEntity(updatedLocation);
    }

    @Transactional
    public void deleteLocation(Long id, Long userId) {
        log.info("Deleting location with id: {} for userId: {}", id, userId);
        Location location = locationRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with id: " + id));

        locationRepository.delete(location);
    }

    @Transactional(readOnly = true)
    public LocationStatsDto getUserStats(Long userId) {
        long totalLocations = locationRepository.countByUserId(userId);
        long totalCountries = locationRepository.countDistinctCountriesByUserId(userId);
        return new LocationStatsDto(totalLocations, totalCountries);
    }
}
