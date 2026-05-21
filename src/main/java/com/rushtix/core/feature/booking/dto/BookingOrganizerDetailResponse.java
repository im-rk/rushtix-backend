package com.rushtix.core.feature.booking.dto;

import com.rushtix.core.domain.enums.BookingStatus;
import com.rushtix.core.feature.seat.dto.SeatResponse;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record BookingOrganizerDetailResponse(
        UUID bookingId,
        UUID eventId,
        String eventTitle,
        String customerName,
        String customerEmail,
        BigDecimal totalAmount,
        BookingStatus status,
        String idempotencyKey,
        OffsetDateTime expiresAt,
        OffsetDateTime confirmedAt,
        OffsetDateTime cancelledAt,
        String cancellationReason,
        OffsetDateTime createdAt,
        List<SeatResponse> seats // Holds individual ticket pricing/scanning data
) {
}
