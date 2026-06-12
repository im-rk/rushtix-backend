package com.rushtix.core.feature.events.dto;

import com.rushtix.core.feature.ticketcategory.dto.PublicTicketCategoryResponse;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record PublicEventDetailsResponse(
        UUID id,
        String title,
        String description,
        String category,
        String imageUrl,
        OffsetDateTime eventDate,
        OffsetDateTime bookingOpensAt,
        OffsetDateTime bookingClosesAt,
        String venueName,
        String venueCity,
        String addressLine, // ◄ Fixed casing to protect MapStruct compilation loops

        List<PublicTicketCategoryResponse> ticketCategories
) {}