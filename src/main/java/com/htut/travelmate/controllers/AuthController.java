package com.htut.travelmate.controllers;

import com.htut.travelmate.dtos.RegisterRequestDto;
import com.htut.travelmate.dtos.UserResponseDto;
import com.htut.travelmate.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for authentication endpoints (registration, login, logout).
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@Valid @RequestBody RegisterRequestDto request) {
        UserResponseDto response = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
