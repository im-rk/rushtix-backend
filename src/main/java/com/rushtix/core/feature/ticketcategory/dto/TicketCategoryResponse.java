package com.rushtix.core.feature.ticketcategory.dto;

import com.rushtix.core.domain.enums.PricingAlgorithm;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Response DTO for Ticket Categories
 * Shows the CURRENT (live) price and available seats
 * Used for both admin and public endpoints
 */
public record TicketCategoryResponse(
        UUID id,
        String name,
        BigDecimal basePrice,
        BigDecimal currentPrice,  // Live price (may differ from base due to dynamic pricing)
        BigDecimal minPrice,
        BigDecimal maxPrice,
        int capacity,
        int availableSeats,        // Calculated: capacity - (seatsSold + seatsLocked)
        int seatsSold,
        int seatsLocked,
        int displayOrder,
        PricingAlgorithm pricingAlgorithm,
        boolean dynamicPricingEnabled
) {}
