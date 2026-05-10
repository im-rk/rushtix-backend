package com.rushtix.core.feature.events.repository;

import com.rushtix.core.domain.entities.Event;
import com.rushtix.core.domain.enums.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {

    List<Event> findAllByOrganizerId(UUID organizerId);
    Optional<Event> findByIdAndOrganizerId(UUID id, UUID organizerId);

    List<Event> findAllByStatusAndEventDateAfter(EventStatus status, OffsetDateTime eventDate);
    List<Event> findAllByStatus(EventStatus status);

    List<Event> findAllByVenueId(UUID venueId);
}
