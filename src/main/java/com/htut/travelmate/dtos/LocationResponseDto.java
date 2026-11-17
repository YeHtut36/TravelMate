package com.htut.travelmate.dtos;

import com.htut.travelmate.entities.Location;
import com.htut.travelmate.enums.LocationCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Response payload representing location details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationResponseDto {

    private Long id;
    private String title;
    private String city;
    private String country;
    private Double latitude;
    private Double longitude;
    private String description;
    private LocationCategory category;
    private LocalDate visitedAt;
    private Instant createdAt;
    private Instant updatedAt;

    public static LocationResponseDto fromEntity(Location location) {
        return LocationResponseDto.builder()
                .id(location.getId())
                .title(location.getTitle())
                .city(location.getCity())
                .country(location.getCountry())
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .description(location.getDescription())
                .category(location.getCategory())
                .visitedAt(location.getVisitedAt())
                .createdAt(location.getCreatedAt())
                .updatedAt(location.getUpdatedAt())
                .build();
    }
}
