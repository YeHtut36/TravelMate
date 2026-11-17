package com.htut.travelmate.dtos;

import com.htut.travelmate.entities.User;
import com.htut.travelmate.enums.Role;

import java.time.Instant;

/**
 * Safe user response DTO strictly omitting sensitive data like password hashes.
 */
public record UserResponseDto(
        Long id,
        String email,
        String fullName,
        Role role,
        Instant createdAt
) {
    public static UserResponseDto fromEntity(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}
