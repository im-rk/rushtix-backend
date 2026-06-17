package com.rushtix.core.feature.venue.dto;

import com.rushtix.core.domain.enums.VenueStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record OrganizerVenueResponse(
        UUID id,
        String name,
        String city,
        String state,
        String addressLine,
        String pincode,
        Integer totalCapacity,
        String timezone,
        BigDecimal latitude,
        BigDecimal longitude,
        String seatMapConfig,
        VenueStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
