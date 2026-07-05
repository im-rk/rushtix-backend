package com.rushtix.core.feature.booking.service;

import com.rushtix.core.domain.entities.Booking;
import com.rushtix.core.domain.enums.BookingStatus;
import com.rushtix.core.feature.booking.dto.BookingOrganizerDetailResponse;
import com.rushtix.core.feature.booking.dto.BookingOrganizerSummaryResponse;
import com.rushtix.core.feature.booking.dto.BookingOrganizerProjection;
import com.rushtix.core.feature.booking.mapper.BookingMapper;
import com.rushtix.core.feature.booking.repository.BookingRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingOrganizerService {
    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;

    @Transactional(readOnly = true)
    public Page<BookingOrganizerSummaryResponse> getEventBookingsDashboard(
            UUID eventId, BookingStatus status, String search, Pageable pageable) {

        String statusStr = status != null ? status.name() : "";
        String searchStr = search != null ? search : "";
        Page<BookingOrganizerProjection> bookingsPage = bookingRepository.findCombinedAdminDashboardBookings(eventId, statusStr, searchStr, pageable);
        return bookingsPage.map(proj -> {
            BookingStatus mappedStatus;
            try {
                mappedStatus = BookingStatus.valueOf(proj.getStatus());
            } catch (IllegalArgumentException e) {
                mappedStatus = BookingStatus.CANCELLED;
            }
            return new BookingOrganizerSummaryResponse(
                    UUID.fromString(proj.getBookingId()),
                    proj.getCustomerName(),
                    proj.getCustomerEmail(),
                    proj.getTicketCount(),
                    proj.getTotalPrice(),
                    mappedStatus,
                    proj.getCreatedAt()
            );
        });
    }

    @Transactional(readOnly = true)
    public BookingOrganizerDetailResponse getBookingDetailsForAudit(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking transaction not found"));

        return bookingMapper.toOrganizerDetail(booking);
    }
}
