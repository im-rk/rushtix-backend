package com.rushtix.core.feature.booking.repository;

import com.rushtix.core.feature.booking.dto.BookingOrganizerProjection;
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

    List<Booking> findAllByUserIdOrderByCreatedAtDesc(UUID userId);

    @Query(value = "SELECT * FROM ( " +
            "SELECT CAST(b.id AS text) as booking_id, u.full_name as customer_name, u.email as customer_email, " +
            "(SELECT count(s.id) FROM seats s WHERE s.booking_id = b.id) as ticket_count, " +
            "b.total_amount as total_price, " +
            "CAST(b.status AS text) as status, " +
            "b.created_at as created_at " +
            "FROM bookings b JOIN users u ON b.user_id = u.id " +
            "WHERE b.event_id = :eventId AND b.status != 'CANCELLED' " +
            "UNION ALL " +
            "SELECT CAST(gb.id AS text) as booking_id, u.full_name as customer_name, u.email as customer_email, " +
            "(SELECT count(pi.id) FROM group_payment_items pi WHERE pi.group_booking_id = gb.id) as ticket_count, " +
            "gb.total_amount as total_price, " +
            "CASE WHEN gb.status = 0 THEN 'PENDING' WHEN gb.status = 1 THEN 'CONFIRMED' ELSE 'FAILED_REJECTED' END as status, " +
            "gb.created_at as created_at " +
            "FROM group_bookings gb JOIN users u ON gb.initiator_user_id = u.id " +
            "WHERE gb.event_id = :eventId AND gb.status != 2 " +
            ") as combined " +
            "WHERE (:status = '' OR combined.status = :status) " +
            "AND (:search = '' OR LOWER(combined.customer_name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(combined.customer_email) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "ORDER BY combined.created_at DESC",
            countQuery = "SELECT count(*) FROM ( " +
            "SELECT CAST(b.id AS text) as booking_id, CAST(b.status AS text) as status, u.full_name as customer_name, u.email as customer_email " +
            "FROM bookings b JOIN users u ON b.user_id = u.id " +
            "WHERE b.event_id = :eventId AND b.status != 'CANCELLED' " +
            "UNION ALL " +
            "SELECT CAST(gb.id AS text) as booking_id, CASE WHEN gb.status = 0 THEN 'PENDING' WHEN gb.status = 1 THEN 'CONFIRMED' ELSE 'FAILED_REJECTED' END as status, u.full_name as customer_name, u.email as customer_email " +
            "FROM group_bookings gb JOIN users u ON gb.initiator_user_id = u.id " +
            "WHERE gb.event_id = :eventId AND gb.status != 2 " +
            ") as combined " +
            "WHERE (:status = '' OR combined.status = :status) " +
            "AND (:search = '' OR LOWER(combined.customer_name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(combined.customer_email) LIKE LOWER(CONCAT('%', :search, '%')))",
            nativeQuery = true)
    Page<BookingOrganizerProjection> findCombinedAdminDashboardBookings(
            @Param("eventId") UUID eventId,
            @Param("status") String status,
            @Param("search") String search,
            Pageable pageable
    );

    List<Booking> findAllByStatusAndExpiresAtBefore(BookingStatus status, java.time.OffsetDateTime now);

    @Query("SELECT DISTINCT b FROM Booking b " +
           "JOIN FETCH b.seats s " +
           "WHERE b.user.id = :userId " +
           "ORDER BY b.createdAt DESC")
    List<Booking> findAllUserBookingsWithSeats(@Param("userId") UUID userId);

}
