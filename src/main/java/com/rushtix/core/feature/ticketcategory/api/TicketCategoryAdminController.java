package com.rushtix.core.feature.ticketcategory.api;

import com.rushtix.core.feature.ticketcategory.dto.TicketCategoryRequest;
import com.rushtix.core.feature.ticketcategory.dto.TicketCategoryResponse;
import com.rushtix.core.feature.ticketcategory.service.TicketCategoryService;
import com.rushtix.core.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizer/events/{eventId}/categories")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ORGANIZER')")
public class TicketCategoryAdminController {

    private final TicketCategoryService ticketCategoryService;

    /**
     * Create a new ticket category (tier) for an event
     * POST /api/v1/organizer/events/{eventId}/categories
     *
     * Example: Add "VIP" tier with capacity 100, basePrice $150
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TicketCategoryResponse createCategory(
            @PathVariable UUID eventId,
            @RequestBody @Valid TicketCategoryRequest request) {
        UUID organizerId = SecurityUtils.getCurrentUserId();
        return ticketCategoryService.createCategory(eventId, organizerId, request);
    }

    /**
     * Update a ticket category
     * PUT /api/v1/organizer/events/{eventId}/categories/{categoryId}
     *
     * Constraints:
     * - Cannot change capacity if seats are sold
     * - Price must maintain: minPrice <= basePrice <= maxPrice
     */
    @PutMapping("/{categoryId}")
    public TicketCategoryResponse updateCategory(
            @PathVariable UUID eventId,
            @PathVariable UUID categoryId,
            @RequestBody @Valid TicketCategoryRequest request) {
        UUID organizerId = SecurityUtils.getCurrentUserId();
        return ticketCategoryService.updateCategory(eventId, organizerId, categoryId, request);
    }

    /**
     * Update the current price for dynamic pricing
     * PATCH /api/v1/organizer/events/{eventId}/categories/{categoryId}/price
     *
     * This is called by the AI pricing engine or manual adjustments
     * Price must be within [minPrice, maxPrice]
     */
    @PatchMapping("/{categoryId}/price")
    public TicketCategoryResponse updatePrice(
            @PathVariable UUID eventId,
            @PathVariable UUID categoryId,
            @RequestParam("newPrice") BigDecimal newPrice) {
        UUID organizerId = SecurityUtils.getCurrentUserId();
        return ticketCategoryService.updateCurrentPrice(eventId, organizerId, categoryId, newPrice);
    }

    /**
     * Delete a ticket category
     * DELETE /api/v1/organizer/events/{eventId}/categories/{categoryId}
     *
     * Constraint: Only allow if no seats have been sold
     */
    @DeleteMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(
            @PathVariable UUID eventId,
            @PathVariable UUID categoryId) {
        UUID organizerId = SecurityUtils.getCurrentUserId();
        ticketCategoryService.deleteCategory(eventId, organizerId, categoryId);
    }
}
