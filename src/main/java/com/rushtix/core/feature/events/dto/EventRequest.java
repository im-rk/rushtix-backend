package com.rushtix.core.feature.events.dto;

import jakarta.validation.constraints.*;

import java.time.OffsetDateTime;
import java.util.UUID;

public record EventRequest(
        @NotBlank(message = "Event title cannot be blank")
        @Size(min = 3, max = 500, message = "Title must be between 3 and 500 characters")
        String title,

        @Size(max = 5000, message = "Description cannot exceed 5000 characters")
        String description,

        @NotBlank(message = "Category cannot be blank")
        @Size(max = 100, message = "Category cannot exceed 100 characters")
        String category,

        @Size(max = 1000, message = "Image URL cannot exceed 1000 characters")
        @Pattern(regexp = "^(https?://.+)?$", message = "Image URL must be a valid HTTP/HTTPS URL or empty")
        String imageUrl,

        @NotNull(message = "Venue ID cannot be null")
        UUID venueId,

        @NotNull(message = "Event date cannot be null")
        @FutureOrPresent(message = "Event date must be in the future or present")
        OffsetDateTime eventDate,

        @FutureOrPresent(message = "Event end date must be in the future or present")
        OffsetDateTime eventEndDate,

        @NotNull(message = "Booking opens at cannot be null")
        OffsetDateTime bookingOpensAt,

        @NotNull(message = "Booking closes at cannot be null")
        OffsetDateTime bookingClosesAt,

        @Min(value = 1, message = "Total seats must be at least 1")
        @Max(value = 100000, message = "Total seats cannot exceed 100000")
        int totalSeats
) {}
