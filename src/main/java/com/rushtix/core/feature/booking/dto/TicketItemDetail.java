package com.rushtix.core.feature.booking.dto;

import java.util.UUID;

public record TicketItemDetail(
    UUID seatId,
    String seatCode,
    String qrToken
) {}
