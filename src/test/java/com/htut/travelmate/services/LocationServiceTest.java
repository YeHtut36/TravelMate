package com.htut.travelmate.services;

import com.htut.travelmate.common.exception.ResourceNotFoundException;
import com.htut.travelmate.dtos.CreateLocationRequestDto;
import com.htut.travelmate.dtos.LocationResponseDto;
import com.htut.travelmate.dtos.LocationStatsDto;
import com.htut.travelmate.dtos.UpdateLocationRequestDto;
import com.htut.travelmate.entities.Location;
import com.htut.travelmate.entities.User;
import com.htut.travelmate.enums.LocationCategory;
import com.htut.travelmate.enums.Role;
import com.htut.travelmate.repositories.LocationRepository;
import com.htut.travelmate.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocationServiceTest {

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private LocationService locationService;

    private User user;
    private Location sampleLocation;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("traveler@example.com")
                .fullName("Traveler")
                .role(Role.ROLE_USER)
                .build();

        sampleLocation = Location.builder()
                .id(100L)
                .title("Grand Canyon")
                .city("Arizona")
                .country("USA")
                .latitude(36.1069)
                .longitude(-112.1129)
                .description("Incredible natural wonder")
                .category(LocationCategory.NATURE)
                .visitedAt(LocalDate.of(2023, 5, 20))
                .user(user)
                .createdAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("Should return all locations mapped to DTOs for a given user")
    void shouldGetUserLocations() {
        when(locationRepository.findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(sampleLocation));

        List<LocationResponseDto> result = locationService.getUserLocations(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Grand Canyon");
        assertThat(result.get(0).getCategory()).isEqualTo(LocationCategory.NATURE);
        verify(locationRepository).findByUserIdOrderByCreatedAtDesc(1L);
    }

    @Test
    @DisplayName("Should return location DTO when found by id and user")
    void shouldGetLocationById() {
        when(locationRepository.findByIdAndUserId(100L, 1L))
                .thenReturn(Optional.of(sampleLocation));

        LocationResponseDto result = locationService.getLocationById(100L, 1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getTitle()).isEqualTo("Grand Canyon");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when location not found for user")
    void shouldThrowWhenLocationNotFound() {
        when(locationRepository.findByIdAndUserId(999L, 1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> locationService.getLocationById(999L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Location not found with id: 999");
    }

    @Test
    @DisplayName("Should create location successfully when user exists")
    void shouldCreateLocation() {
        CreateLocationRequestDto requestDto = CreateLocationRequestDto.builder()
                .title("Machu Picchu")
                .city("Cusco")
                .country("Peru")
                .latitude(-13.1631)
                .longitude(-72.5450)
                .description("Ancient Incan citadel")
                .category(LocationCategory.HISTORICAL)
                .visitedAt(LocalDate.of(2022, 9, 15))
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(locationRepository.save(any(Location.class))).thenAnswer(invocation -> {
            Location toSave = invocation.getArgument(0);
            toSave.setId(101L);
            toSave.setCreatedAt(Instant.now());
            return toSave;
        });

        LocationResponseDto created = locationService.createLocation(requestDto, 1L);

        assertThat(created).isNotNull();
        assertThat(created.getId()).isEqualTo(101L);
        assertThat(created.getTitle()).isEqualTo("Machu Picchu");
        assertThat(created.getCategory()).isEqualTo(LocationCategory.HISTORICAL);
        verify(locationRepository).save(any(Location.class));
    }

    @Test
    @DisplayName("Should update existing location successfully")
    void shouldUpdateLocation() {
        UpdateLocationRequestDto updateDto = UpdateLocationRequestDto.builder()
                .title("Grand Canyon South Rim")
                .city("Arizona")
                .country("USA")
                .latitude(36.0544)
                .longitude(-112.1401)
                .description("Updated description")
                .category(LocationCategory.ADVENTURE)
                .visitedAt(LocalDate.of(2023, 6, 1))
                .build();

        when(locationRepository.findByIdAndUserId(100L, 1L))
                .thenReturn(Optional.of(sampleLocation));
        when(locationRepository.save(any(Location.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LocationResponseDto updated = locationService.updateLocation(100L, updateDto, 1L);

        assertThat(updated.getTitle()).isEqualTo("Grand Canyon South Rim");
        assertThat(updated.getCategory()).isEqualTo(LocationCategory.ADVENTURE);
        verify(locationRepository).save(sampleLocation);
    }

    @Test
    @DisplayName("Should delete location when owned by user")
    void shouldDeleteLocation() {
        when(locationRepository.findByIdAndUserId(100L, 1L))
                .thenReturn(Optional.of(sampleLocation));

        locationService.deleteLocation(100L, 1L);

        verify(locationRepository).delete(sampleLocation);
    }

    @Test
    @DisplayName("Should return user stats")
    void shouldGetUserStats() {
        when(locationRepository.countByUserId(1L)).thenReturn(5L);
        when(locationRepository.countDistinctCountriesByUserId(1L)).thenReturn(3L);

        LocationStatsDto stats = locationService.getUserStats(1L);

        assertThat(stats.getTotalLocations()).isEqualTo(5L);
        assertThat(stats.getTotalCountries()).isEqualTo(3L);
    }
}
