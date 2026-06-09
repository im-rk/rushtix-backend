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

    @GetMapping
    public List<TicketCategoryResponse> getEventCategories(@PathVariable UUID eventId) {
        return ticketCategoryService.getCategoriesForEvent(eventId);
    }

    @GetMapping("/{categoryId}")
    public TicketCategoryResponse getCategoryDetails(
            @PathVariable UUID eventId,
            @PathVariable UUID categoryId) {
        return ticketCategoryService.getCategoryById(categoryId);
    }
}
