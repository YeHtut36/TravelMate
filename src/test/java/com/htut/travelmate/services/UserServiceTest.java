package com.htut.travelmate.services;

import com.htut.travelmate.common.exception.DuplicateResourceException;
import com.htut.travelmate.dtos.RegisterRequestDto;
import com.htut.travelmate.dtos.UserResponseDto;
import com.htut.travelmate.entities.User;
import com.htut.travelmate.enums.Role;
import com.htut.travelmate.repositories.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("Should successfully register a new user and encode password")
    void shouldRegisterUserSuccessfully() {
        RegisterRequestDto request = new RegisterRequestDto(
                "Traveler@Example.COM",
                "SecretPass123",
                "Jane Traveler"
        );

        when(userRepository.existsByEmail("traveler@example.com")).thenReturn(false);
        when(passwordEncoder.encode("SecretPass123")).thenReturn("$2a$10$encodedHash");

        User savedUser = User.builder()
                .id(1L)
                .email("traveler@example.com")
                .passwordHash("$2a$10$encodedHash")
                .fullName("Jane Traveler")
                .role(Role.ROLE_USER)
                .createdAt(Instant.now())
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponseDto response = userService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("traveler@example.com");
        assertThat(response.fullName()).isEqualTo("Jane Traveler");
        assertThat(response.role()).isEqualTo(Role.ROLE_USER);

        verify(userRepository).existsByEmail("traveler@example.com");
        verify(passwordEncoder).encode("SecretPass123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when email already exists")
    void shouldThrowWhenEmailAlreadyExists() {
        RegisterRequestDto request = new RegisterRequestDto(
                "existing@example.com",
                "SecretPass123",
                "Existing User"
        );

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("An account with this email already exists");

        verify(userRepository).existsByEmail("existing@example.com");
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }
}
