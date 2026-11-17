package com.htut.travelmate.repositories;

import com.htut.travelmate.entities.Location;
import com.htut.travelmate.entities.User;
import com.htut.travelmate.enums.LocationCategory;
import com.htut.travelmate.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class LocationRepositoryTest {

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private UserRepository userRepository;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        user1 = userRepository.save(User.builder()
                .email("user1@example.com")
                .passwordHash("hashedPass1")
                .fullName("Alice")
                .role(Role.ROLE_USER)
                .build());

        user2 = userRepository.save(User.builder()
                .email("user2@example.com")
                .passwordHash("hashedPass2")
                .fullName("Bob")
                .role(Role.ROLE_USER)
                .build());
    }

    @Test
    @DisplayName("Should find locations belonging only to specific user, ordered by creation")
    void shouldFindLocationsByUserId() {
        Location loc1 = locationRepository.save(Location.builder()
                .title("Eiffel Tower")
                .city("Paris")
                .country("France")
                .category(LocationCategory.HISTORICAL)
                .user(user1)
                .build());

        Location loc2 = locationRepository.save(Location.builder()
                .title("Louvre Museum")
                .city("Paris")
                .country("France")
                .category(LocationCategory.CULTURE)
                .user(user1)
                .build());

        locationRepository.save(Location.builder()
                .title("Colosseum")
                .city("Rome")
                .country("Italy")
                .category(LocationCategory.HISTORICAL)
                .user(user2)
                .build());

        List<Location> user1Locations = locationRepository.findByUserIdOrderByCreatedAtDesc(user1.getId());
        assertThat(user1Locations).hasSize(2);
        assertThat(user1Locations).extracting(Location::getTitle)
                .containsExactlyInAnyOrder("Eiffel Tower", "Louvre Museum");
    }

    @Test
    @DisplayName("Should enforce resource isolation: user cannot find another user's location by id")
    void shouldEnforceUserIsolation() {
        Location loc = locationRepository.save(Location.builder()
                .title("Bagan")
                .city("Mandalay")
                .country("Myanmar")
                .category(LocationCategory.HISTORICAL)
                .user(user1)
                .build());

        Optional<Location> foundForUser1 = locationRepository.findByIdAndUserId(loc.getId(), user1.getId());
        assertThat(foundForUser1).isPresent();

        Optional<Location> foundForUser2 = locationRepository.findByIdAndUserId(loc.getId(), user2.getId());
        assertThat(foundForUser2).isEmpty();
    }

    @Test
    @DisplayName("Should count locations and distinct countries per user")
    void shouldCountStatsPerUser() {
        locationRepository.save(Location.builder()
                .title("Kyoto Temple")
                .city("Kyoto")
                .country("Japan")
                .category(LocationCategory.CULTURE)
                .user(user1)
                .build());

        locationRepository.save(Location.builder()
                .title("Mount Fuji")
                .city("Shizuoka")
                .country("Japan")
                .category(LocationCategory.NATURE)
                .user(user1)
                .build());

        locationRepository.save(Location.builder()
                .title("Santorini")
                .city("Thira")
                .country("Greece")
                .category(LocationCategory.BEACH)
                .user(user1)
                .build());

        assertThat(locationRepository.countByUserId(user1.getId())).isEqualTo(3);
        assertThat(locationRepository.countDistinctCountriesByUserId(user1.getId())).isEqualTo(2);
    }
}
