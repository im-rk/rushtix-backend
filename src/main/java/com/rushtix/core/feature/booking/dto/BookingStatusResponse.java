package com.rushtix.core.feature.booking.dto;

import com.rushtix.core.domain.enums.BookingStatus;
import io.lettuce.core.BitFieldArgs;

import java.time.OffsetDateTime;
import java.util.List;

public record BookingStatusResponse(
        BookingStatus status,
        String eventTitle,
        OffsetDateTime eventDateTime,
        String venueName,
        String venueCity,
        List<TicketPassResponse> tickets
) {}
