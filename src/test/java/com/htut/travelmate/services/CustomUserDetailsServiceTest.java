package com.htut.travelmate.services;

import com.htut.travelmate.entities.User;
import com.htut.travelmate.enums.Role;
import com.htut.travelmate.repositories.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    @Test
    @DisplayName("Should load UserDetails by email successfully")
    void shouldLoadUserByUsername() {
        User user = User.builder()
                .id(1L)
                .email("traveler@example.com")
                .passwordHash("$2a$10$hashedPassword")
                .fullName("Jane Traveler")
                .role(Role.ROLE_USER)
                .build();

        when(userRepository.findByEmail("traveler@example.com")).thenReturn(Optional.of(user));

        UserDetails userDetails = userDetailsService.loadUserByUsername("Traveler@example.COM");

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("traveler@example.com");
        assertThat(userDetails.getPassword()).isEqualTo("$2a$10$hashedPassword");
        assertThat(userDetails.getAuthorities()).extracting("authority").containsExactly("ROLE_USER");

        verify(userRepository).findByEmail("traveler@example.com");
    }

    @Test
    @DisplayName("Should throw UsernameNotFoundException when user email does not exist")
    void shouldThrowWhenUserNotFound() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("unknown@example.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found with email: unknown@example.com");
    }
}
