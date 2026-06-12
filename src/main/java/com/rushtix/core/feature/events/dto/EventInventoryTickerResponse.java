package com.rushtix.core.feature.events.dto;

import java.util.UUID;

public record EventInventoryTickerResponse(
        UUID id,
        int availableSeats,
        boolean isSoldOut,
        boolean isBookingOpen
) {}
