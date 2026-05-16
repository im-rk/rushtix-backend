package com.rushtix.core.feature.seat.dto;

import com.rushtix.core.domain.enums.SeatStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SeatUpdateRequest(
        @NotBlank(message = "Row label is required")
        @Size(max = 10)
        String rowLabel,

        @NotBlank(message = "Seat number is required")
        @Size(max = 10)
        String seatNumber,

        @NotNull(message = "Accessibility status is required")
        SeatStatus status,

        boolean isAccessible
) {}
