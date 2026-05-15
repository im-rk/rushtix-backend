package com.rushtix.core.feature.seat.dto;

import jakarta.validation.constraints.*;

import java.util.UUID;

public record SeatBulkCreateRequest(
        @NotNull(message = "Category ID is required")
        UUID categoryId,

        @NotBlank(message = "Row label is required")
        @Size(max = 10)
        String rowLabel,

        @Min(1)
        int startNumber,

        @Min(1)
        @Max(100) // Safety limit per row
        int count,

        boolean isAccessible
) {}