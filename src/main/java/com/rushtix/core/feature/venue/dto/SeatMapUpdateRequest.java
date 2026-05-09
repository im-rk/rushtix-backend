package com.rushtix.core.feature.venue.dto;

import jakarta.validation.constraints.NotBlank;

public record SeatMapUpdateRequest(
        @NotBlank(message = "Seat map configuration cannot be empty")
        String seatMapConfig
) {}
