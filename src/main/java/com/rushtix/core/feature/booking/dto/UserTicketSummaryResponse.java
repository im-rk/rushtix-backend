package com.rushtix.core.feature.booking.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record UserTicketSummaryResponse(
    UUID bookingId,
    String eventTitle,
    String venueName,
    OffsetDateTime eventDate,
    String status,
    BigDecimal totalAmount,
    List<TicketItemDetail> tickets
) {}
