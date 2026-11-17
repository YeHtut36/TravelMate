package com.htut.travelmate.repositories;

import com.htut.travelmate.entities.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for Location entity operations with user-scoped queries.
 */
@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {

    List<Location> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Location> findByIdAndUserId(Long id, Long userId);

    boolean existsByIdAndUserId(Long id, Long userId);

    long countByUserId(Long userId);

    @Query("SELECT COUNT(DISTINCT l.country) FROM Location l WHERE l.user.id = :userId")
    long countDistinctCountriesByUserId(@Param("userId") Long userId);
}
