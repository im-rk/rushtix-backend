package com.rushtix.core.feature.booking.dto;

import com.rushtix.core.domain.enums.BookingStatus;
import com.rushtix.core.feature.seat.dto.SeatResponse;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record BookingUserResponse(
        UUID bookingId,
        UUID eventId,
        String eventTitle,
        BookingStatus status,
        BigDecimal totalAmount,
        OffsetDateTime expiresAt,
        String paymentClientSecret, // Gateway-ready placeholder
        String gatewayType,         // Gateway-ready placeholder
        List<SeatResponse> seats
) {}
