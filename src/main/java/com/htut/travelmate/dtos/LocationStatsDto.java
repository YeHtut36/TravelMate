package com.htut.travelmate.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Summary statistics for a user's travel dashboard.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationStatsDto {
    private long totalLocations;
    private long totalCountries;
}
