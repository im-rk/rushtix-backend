package com.rushtix.core.feature.seat.dto;

import com.rushtix.core.domain.enums.SeatStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record SeatResponse(
        UUID id,
        UUID eventId,
        UUID cateqoryId,
        String rowLabel,
        String seatNumber,
        String displayLabel,
        boolean isAccessible,
        SeatStatus status,
        UUID lockedById,
        UUID bookedById,
        UUID bookingId,
        OffsetDateTime lockedUntil
) {
}
