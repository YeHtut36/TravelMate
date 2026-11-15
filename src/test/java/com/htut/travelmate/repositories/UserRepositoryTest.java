package com.htut.travelmate.repositories;

import com.htut.travelmate.entities.User;
import com.htut.travelmate.enums.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Should save a user and find by email successfully")
    void shouldSaveAndFindByEmail() {
        User user = User.builder()
                .email("traveler@example.com")
                .passwordHash("$2a$10$hashedPasswordSample")
                .fullName("John Doe")
                .role(Role.ROLE_USER)
                .build();

        User savedUser = userRepository.save(user);

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getCreatedAt()).isNotNull();

        Optional<User> found = userRepository.findByEmail("traveler@example.com");
        assertThat(found).isPresent();
        assertThat(found.get().getFullName()).isEqualTo("John Doe");
        assertThat(found.get().getRole()).isEqualTo(Role.ROLE_USER);
    }

    @Test
    @DisplayName("Should return true when email exists and false when it does not")
    void shouldCheckIfEmailExists() {
        User user = User.builder()
                .email("alice@example.com")
                .passwordHash("$2a$10$hashedPasswordSample")
                .fullName("Alice")
                .role(Role.ROLE_USER)
                .build();
        userRepository.save(user);

        assertThat(userRepository.existsByEmail("alice@example.com")).isTrue();
        assertThat(userRepository.existsByEmail("bob@example.com")).isFalse();
    }

    @Test
    @DisplayName("Should fail when saving duplicate email due to unique constraint")
    void shouldFailOnDuplicateEmail() {
        User user1 = User.builder()
                .email("duplicate@example.com")
                .passwordHash("hash1")
                .fullName("User One")
                .role(Role.ROLE_USER)
                .build();
        userRepository.saveAndFlush(user1);

        User user2 = User.builder()
                .email("duplicate@example.com")
                .passwordHash("hash2")
                .fullName("User Two")
                .role(Role.ROLE_USER)
                .build();

        assertThatThrownBy(() -> userRepository.saveAndFlush(user2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
