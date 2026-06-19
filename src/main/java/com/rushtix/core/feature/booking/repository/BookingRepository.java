package com.rushtix.core.feature.booking.repository;

import com.rushtix.core.domain.entities.Booking;
import com.rushtix.core.domain.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {

    @Query("SELECT b FROM Booking b WHERE b.event.id = :eventId " +
            "AND (:status IS NULL OR b.status = :status) " +
            "AND (:search IS NULL OR LOWER(b.user.fullName) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')) " +
            "OR LOWER(b.user.email) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')))")
    Page<Booking> findAdminDashboardBookings(
            @Param("eventId") UUID eventId,
            @Param("status") BookingStatus status,
            @Param("search") String search,
            Pageable pageable
    );

    List<Booking> findAllByUserIdOrderByCreatedAtDesc(UUID userId);

    List<Booking> findAllByStatusAndExpiresAtBefore(BookingStatus status, java.time.OffsetDateTime now);

}
