package com.rushtix.core.feature.booking.dto;

import com.rushtix.core.domain.enums.BookingStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record BookingOrganizerSummaryResponse(
        UUID bookingId,
        String customerName,
        String customerEmail,
        int ticketCount,
        BigDecimal totalPrice,
        BookingStatus status,
        OffsetDateTime createdAt
) {
}
