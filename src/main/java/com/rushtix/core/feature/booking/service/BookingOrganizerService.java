package com.rushtix.core.feature.booking.service;

import com.rushtix.core.domain.entities.Booking;
import com.rushtix.core.domain.enums.BookingStatus;
import com.rushtix.core.feature.booking.dto.BookingOrganizerDetailResponse;
import com.rushtix.core.feature.booking.dto.BookingOrganizerSummaryResponse;
import com.rushtix.core.feature.booking.mapper.BookingMapper;
import com.rushtix.core.feature.booking.repository.BookingRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.awt.print.Pageable;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingOrganizerService {
    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;

    @Transactional(readOnly = true)
    public Page<BookingOrganizerSummaryResponse> getEventBookingsDashboard(
            UUID eventId, BookingStatus status, String search, Pageable pageable) {

        Page<Booking> bookingsPage = bookingRepository.findAdminDashboardBookings(eventId, status, search, pageable);
        return bookingsPage.map(bookingMapper::toOrganizerSummary);
    }

    @Transactional(readOnly = true)
    public BookingOrganizerDetailResponse getBookingDetailsForAudit(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking transaction not found"));

        return bookingMapper.toOrganizerDetail(booking);
    }
}
