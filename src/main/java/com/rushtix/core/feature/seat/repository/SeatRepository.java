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
}
