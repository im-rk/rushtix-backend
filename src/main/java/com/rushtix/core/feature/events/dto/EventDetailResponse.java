package com.rushtix.core.feature.events.dto;

import com.rushtix.core.domain.enums.EventStatus;
import com.rushtix.core.feature.venue.dto.OrganizerVenueResponse;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Full DTO for viewing single event details (event detail page)
 * Used in GET /api/v1/events/{id} endpoint
 * Includes full venue details nested
 */
public record EventDetailResponse(
        UUID id,
        String title,
        String description,
        String category,
        String imageUrl,
        OffsetDateTime eventDate,
        OffsetDateTime eventEndDate,
        OffsetDateTime bookingOpensAt,
        OffsetDateTime bookingClosesAt,
        EventStatus status,
        int totalSeats,
        int seatsSold,
        int seatsLocked,
        int availableSeats,
        OrganizerVenueResponse venue,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
