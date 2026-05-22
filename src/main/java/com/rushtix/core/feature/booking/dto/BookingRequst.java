package com.rushtix.core.feature.booking.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record BookingRequst(
        @NotNull(message = "Event ID is required")
        UUID eventId,

        @NotNull(message = "You must select at least one seat")
        List<UUID> seatIds,

        @NotNull(message = "Idempotency key is required to prevent accidental double-booking")
        String idempotencyKey
) {}


