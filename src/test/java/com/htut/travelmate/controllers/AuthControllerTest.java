package com.htut.travelmate.controllers;

import tools.jackson.databind.ObjectMapper;
import com.htut.travelmate.common.config.SecurityConfig;
import com.htut.travelmate.common.exception.DuplicateResourceException;
import com.htut.travelmate.common.exception.GlobalExceptionHandler;
import com.htut.travelmate.dtos.LoginRequestDto;
import com.htut.travelmate.dtos.RegisterRequestDto;
import com.htut.travelmate.dtos.UserResponseDto;
import com.htut.travelmate.enums.Role;
import com.htut.travelmate.services.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @Test
    @DisplayName("POST /api/auth/register - Should return 201 Created when request is valid")
    void shouldRegisterSuccessfully() throws Exception {
        RegisterRequestDto request = new RegisterRequestDto(
                "traveler@example.com",
                "Password123!",
                "Jane Traveler"
        );

        UserResponseDto response = new UserResponseDto(
                1L,
                "traveler@example.com",
                "Jane Traveler",
                Role.ROLE_USER,
                Instant.now()
        );

        when(userService.register(any(RegisterRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("traveler@example.com"))
                .andExpect(jsonPath("$.fullName").value("Jane Traveler"))
                .andExpect(jsonPath("$.role").value("ROLE_USER"));
    }

    @Test
    @DisplayName("POST /api/auth/register - Should return 400 Bad Request when validation fails")
    void shouldReturn400WhenValidationFails() throws Exception {
        RegisterRequestDto invalidRequest = new RegisterRequestDto(
                "invalid-email",
                "123",
                ""
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.password").exists())
                .andExpect(jsonPath("$.errors.fullName").exists());
    }

    @Test
    @DisplayName("POST /api/auth/register - Should return 409 Conflict when email already exists")
    void shouldReturn409WhenEmailAlreadyExists() throws Exception {
        RegisterRequestDto request = new RegisterRequestDto(
                "duplicate@example.com",
                "Password123!",
                "Jane Traveler"
        );

        when(userService.register(any(RegisterRequestDto.class)))
                .thenThrow(new DuplicateResourceException("An account with this email already exists: duplicate@example.com"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Duplicate Resource"))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value("An account with this email already exists: duplicate@example.com"));
    }

    @Test
    @DisplayName("POST /api/auth/login - Should return 200 OK and user details on valid credentials")
    void shouldLoginSuccessfully() throws Exception {
        LoginRequestDto request = new LoginRequestDto("traveler@example.com", "Password123!");
        UserResponseDto response = new UserResponseDto(
                1L, "traveler@example.com", "Jane Traveler", Role.ROLE_USER, Instant.now()
        );

        Authentication auth = new UsernamePasswordAuthenticationToken(
                "traveler@example.com", null, Collections.emptyList()
        );
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(auth);
        when(userService.getByEmail("traveler@example.com")).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("traveler@example.com"))
                .andExpect(jsonPath("$.fullName").value("Jane Traveler"));
    }

    @Test
    @DisplayName("POST /api/auth/login - Should return 401 Unauthorized when credentials are bad")
    void shouldReturn401OnBadCredentials() throws Exception {
        LoginRequestDto request = new LoginRequestDto("traveler@example.com", "WrongPassword");

        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Authentication Failed"))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.detail").value("Invalid email or password"));
    }

    @Test
    @DisplayName("POST /api/auth/logout - Should return 200 OK")
    void shouldLogoutSuccessfully() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk());
    }
}
