package com.rushtix.core.feature.booking.api;

import com.rushtix.core.domain.enums.BookingStatus;
import com.rushtix.core.feature.booking.dto.BookingOrganizerDetailResponse;
import com.rushtix.core.feature.booking.dto.BookingOrganizerSummaryResponse;
import com.rushtix.core.feature.booking.service.BookingOrganizerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizer")
@RequiredArgsConstructor
public class BookingOrganizerController {
    private final BookingOrganizerService bookingOrganizerService;

    @GetMapping("/events/{eventId}/bookings")
    public Page<BookingOrganizerSummaryResponse> getEventBookings(
            @PathVariable UUID eventId,
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {

        return bookingOrganizerService.getEventBookingsDashboard(eventId, status, search, pageable);
    }

    @GetMapping("/bookings/{bookingId}")
    public BookingOrganizerDetailResponse getBookingDetails(@PathVariable UUID bookingId) {
        return bookingOrganizerService.getBookingDetailsForAudit(bookingId);
    }
}
