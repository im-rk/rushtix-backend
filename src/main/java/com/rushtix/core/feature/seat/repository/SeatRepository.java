package com.rushtix.core.feature.seat.repository;

import com.rushtix.core.domain.entities.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SeatRepository extends JpaRepository<Seat, UUID> {
    boolean existsByEventIdAndRowLabelAndSeatNumber(UUID eventId, String rowLabel, String seatNumber);
    List<Seat> findAllByEventIdOrderByRowLabelAscSeatNumberAsc(UUID eventId);

    // Used to prevent changing a seat's label to one that already exists elsewhere
    boolean existsByEventIdAndRowLabelAndSeatNumberAndIdNot(UUID eventId, String rowLabel, String seatNumber, UUID seatId);

    // Used to check if an event has any active commitments (Locked or Sold seats)
    boolean existsByEventIdAndStatusIn(UUID eventId, java.util.Collection<com.rushtix.core.domain.enums.SeatStatus> statuses);

    // The hard clear execution query
    void deleteAllByEventId(UUID eventId);
}
