package com.rushtix.core.feature.booking.repository;

import com.rushtix.core.domain.entities.Booking;
import com.rushtix.core.domain.enums.BookingStatus;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.awt.print.Pageable;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

    @Query("SELECT b FROM Booking b WHERE b.event.id = :eventId " +
            "AND (:status IS NULL || b.status = :status) " +
            "AND (:search IS NULL || LOWER(b.user.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "|| LOWER(b.user.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Booking> findAdminDashboardBookings(
            @Param("eventId") UUID eventId,
            @Param("status") BookingStatus status,
            @Param("search") String search,
            Pageable pageable
    );
}
