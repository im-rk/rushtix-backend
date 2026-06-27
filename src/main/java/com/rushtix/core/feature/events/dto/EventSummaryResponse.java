package com.rushtix.core.feature.events.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Lightweight DTO for browsing all events (public listing page)
 * Used in GET /api/v1/events endpoint
 */
public record EventSummaryResponse(
        UUID id,
        String title,
        String category,
        String imageUrl,
        OffsetDateTime eventDate,
        String venueName,
        String cityName,
        java.math.BigDecimal startingPrice
) {}
