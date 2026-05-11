package com.rushtix.core.feature.ticketcategory.api;

import com.rushtix.core.feature.ticketcategory.dto.TicketCategoryResponse;
import com.rushtix.core.feature.ticketcategory.service.TicketCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/events/{eventId}/categories")
@RequiredArgsConstructor
public class TicketCategoryPublicController {

    private final TicketCategoryService ticketCategoryService;

    /**
     * Get all ticket categories for an event (in display order)
     * GET /api/v1/events/{eventId}/categories
     *
     * Returns: List of ticket tiers with CURRENT (live) prices
     * Order: VIP -> Gold -> Silver -> General (based on displayOrder)
     * Used by: Front-end to show available ticket options
     */
    @GetMapping
    public List<TicketCategoryResponse> getEventCategories(@PathVariable UUID eventId) {
        return ticketCategoryService.getCategoriesForEvent(eventId);
    }

    /**
     * Get details of a single ticket category
     * GET /api/v1/events/{eventId}/categories/{categoryId}
     *
     * Shows: Current price, available seats, pricing algorithm
     * Used by: Front-end to display detailed tier info before purchase
     */
    @GetMapping("/{categoryId}")
    public TicketCategoryResponse getCategoryDetails(
            @PathVariable UUID eventId,
            @PathVariable UUID categoryId) {
        return ticketCategoryService.getCategoryById(categoryId);
    }
}
