package com.htut.travelmate.controllers;

import tools.jackson.databind.ObjectMapper;
import com.htut.travelmate.common.config.SecurityConfig;
import com.htut.travelmate.common.exception.GlobalExceptionHandler;
import com.htut.travelmate.common.exception.ResourceNotFoundException;
import com.htut.travelmate.common.security.UserPrincipal;
import com.htut.travelmate.dtos.CreateLocationRequestDto;
import com.htut.travelmate.dtos.LocationResponseDto;
import com.htut.travelmate.dtos.LocationStatsDto;
import com.htut.travelmate.dtos.UpdateLocationRequestDto;
import com.htut.travelmate.enums.LocationCategory;
import com.htut.travelmate.enums.Role;
import com.htut.travelmate.services.CustomUserDetailsService;
import com.htut.travelmate.services.LocationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LocationController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class LocationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LocationService locationService;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private UserPrincipal userPrincipal;
    private Authentication auth;

    @BeforeEach
    void setUp() {
        userPrincipal = new UserPrincipal(1L, "traveler@example.com", "pass", "Traveler", Role.ROLE_USER);
        auth = new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());
    }

    @Test
    @DisplayName("GET /api/locations without auth should return 401 Unauthorized")
    void shouldReturn401WhenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/locations"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/locations with auth should return user's locations")
    void shouldReturnUserLocations() throws Exception {
        LocationResponseDto locDto = LocationResponseDto.builder()
                .id(10L)
                .title("Mount Fuji")
                .city("Shizuoka")
                .country("Japan")
                .category(LocationCategory.NATURE)
                .createdAt(Instant.now())
                .build();

        when(locationService.getUserLocations(1L)).thenReturn(List.of(locDto));

        mockMvc.perform(get("/api/locations").with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10L))
                .andExpect(jsonPath("$[0].title").value("Mount Fuji"))
                .andExpect(jsonPath("$[0].city").value("Shizuoka"))
                .andExpect(jsonPath("$[0].country").value("Japan"))
                .andExpect(jsonPath("$[0].category").value("NATURE"));
    }

    @Test
    @DisplayName("GET /api/locations/{id} should return 200 when location exists")
    void shouldReturnLocationById() throws Exception {
        LocationResponseDto locDto = LocationResponseDto.builder()
                .id(10L)
                .title("Bagan")
                .city("Mandalay")
                .country("Myanmar")
                .category(LocationCategory.HISTORICAL)
                .build();

        when(locationService.getLocationById(10L, 1L)).thenReturn(locDto);

        mockMvc.perform(get("/api/locations/10").with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.title").value("Bagan"));
    }

    @Test
    @DisplayName("GET /api/locations/{id} should return 404 when location does not exist")
    void shouldReturn404WhenLocationNotFound() throws Exception {
        when(locationService.getLocationById(999L, 1L))
                .thenThrow(new ResourceNotFoundException("Location not found with id: 999"));

        mockMvc.perform(get("/api/locations/999").with(authentication(auth)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Location not found with id: 999"));
    }

    @Test
    @DisplayName("POST /api/locations should return 201 Created on valid payload")
    void shouldCreateLocationSuccessfully() throws Exception {
        CreateLocationRequestDto requestDto = CreateLocationRequestDto.builder()
                .title("Santorini Sunset")
                .city("Oia")
                .country("Greece")
                .category(LocationCategory.BEACH)
                .latitude(36.4618)
                .longitude(25.3753)
                .description("Breathtaking views")
                .visitedAt(LocalDate.of(2023, 7, 10))
                .build();

        LocationResponseDto createdDto = LocationResponseDto.builder()
                .id(20L)
                .title("Santorini Sunset")
                .city("Oia")
                .country("Greece")
                .category(LocationCategory.BEACH)
                .build();

        when(locationService.createLocation(any(CreateLocationRequestDto.class), eq(1L)))
                .thenReturn(createdDto);

        mockMvc.perform(post("/api/locations")
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(20L))
                .andExpect(jsonPath("$.title").value("Santorini Sunset"));
    }

    @Test
    @DisplayName("POST /api/locations with invalid input should return 400 Bad Request")
    void shouldReturn400OnValidationFailure() throws Exception {
        CreateLocationRequestDto invalidDto = CreateLocationRequestDto.builder()
                .title("") // Blank title
                .city("Oia")
                .country("Greece")
                .category(null) // Null category
                .build();

        mockMvc.perform(post("/api/locations")
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.title").exists())
                .andExpect(jsonPath("$.errors.category").exists());
    }

    @Test
    @DisplayName("PUT /api/locations/{id} should return 200 OK on update")
    void shouldUpdateLocation() throws Exception {
        UpdateLocationRequestDto requestDto = UpdateLocationRequestDto.builder()
                .title("Santorini Volcano")
                .city("Fira")
                .country("Greece")
                .category(LocationCategory.ADVENTURE)
                .build();

        LocationResponseDto updatedDto = LocationResponseDto.builder()
                .id(20L)
                .title("Santorini Volcano")
                .city("Fira")
                .country("Greece")
                .category(LocationCategory.ADVENTURE)
                .build();

        when(locationService.updateLocation(eq(20L), any(UpdateLocationRequestDto.class), eq(1L)))
                .thenReturn(updatedDto);

        mockMvc.perform(put("/api/locations/20")
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Santorini Volcano"));
    }

    @Test
    @DisplayName("DELETE /api/locations/{id} should return 204 No Content")
    void shouldDeleteLocation() throws Exception {
        doNothing().when(locationService).deleteLocation(20L, 1L);

        mockMvc.perform(delete("/api/locations/20").with(authentication(auth)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /api/locations/stats should return stats for user")
    void shouldReturnUserStats() throws Exception {
        when(locationService.getUserStats(1L)).thenReturn(new LocationStatsDto(12L, 4L));

        mockMvc.perform(get("/api/locations/stats").with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalLocations").value(12L))
                .andExpect(jsonPath("$.totalCountries").value(4L));
    }
}
