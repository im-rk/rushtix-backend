package com.rushtix.core.feature.booking.dto;

import com.rushtix.core.domain.enums.BookingStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record BookingReservationResponse(
        UUID bookingId,
        BookingStatus status,
        OffsetDateTime expiresAt,
        java.math.BigDecimal totalAmount
) {
}
