package com.rushtix.core.repositories;

import com.rushtix.core.domain.entities.Seat;
import com.rushtix.core.domain.enums.SeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SeatRepository extends JpaRepository<Seat, UUID> {
    List<Seat> findByEventAndCategoryIdAndStatus(UUID eventId, UUID categoryId, SeatStatus status);
}
