package com.rushtix.core.feature.venue.repository;

import com.rushtix.core.domain.entities.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VenueRepository extends JpaRepository<Venue, UUID> {
    List<Venue> findAllByOrganizerId(UUID organizerId);
    Optional<Venue> findByIdAndOrganizerId(UUID id, UUID organizerId);
}
