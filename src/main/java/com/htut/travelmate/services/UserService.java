package com.htut.travelmate.services;

import com.htut.travelmate.common.exception.DuplicateResourceException;
import com.htut.travelmate.dtos.RegisterRequestDto;
import com.htut.travelmate.dtos.UserResponseDto;
import com.htut.travelmate.entities.User;
import com.htut.travelmate.enums.Role;
import com.htut.travelmate.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service managing user business logic and registration.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponseDto register(RegisterRequestDto request) {
        String normalizedEmail = request.email().trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateResourceException("An account with this email already exists: " + normalizedEmail);
        }

        User user = User.builder()
                .email(normalizedEmail)
                .passwordHash(passwordEncoder.encode(request.password()))
                .fullName(request.fullName().trim())
                .role(Role.ROLE_USER)
                .build();

        User savedUser = userRepository.save(user);
        log.info("Registered new user with id: {}", savedUser.getId());

        return UserResponseDto.fromEntity(savedUser);
    }
}
