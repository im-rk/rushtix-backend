package com.rushtix.core.feature.events.api;

import com.rushtix.core.domain.enums.EventStatus;
import com.rushtix.core.feature.events.dto.EventDetailResponse;
import com.rushtix.core.feature.events.dto.EventSummaryResponse;
import com.rushtix.core.feature.events.service.EventPublicService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class EventPublicController {

    private final EventPublicService eventPublicService;

    /**
     * Browse all upcoming published events
     * GET /api/v1/events
     * Returns: List of event summaries (lightweight)
     */
    @GetMapping
    public List<EventSummaryResponse> getAllUpcomingEvents() {
        return eventPublicService.getAllUpcomingEvents();
    }

    /**
     * Get single event details (public)
     * GET /api/v1/events/{id}
     * Returns: Full event details with nested venue information
     */
    @GetMapping("/{id}")
    public EventDetailResponse getEventDetails(@PathVariable UUID id) {
        return eventPublicService.getEventById(id);
    }

    /**
     * Get events by status filter
     * GET /api/v1/events/status/{status}
     */
    @GetMapping("/status/{status}")
    public List<EventSummaryResponse> getEventsByStatus(@PathVariable EventStatus status) {
        return eventPublicService.getEventsByStatus(status);
    }

    /**
     * Get all events at a specific venue
     * GET /api/v1/events/venue/{venueId}
     */
    @GetMapping("/venue/{venueId}")
    public List<EventSummaryResponse> getEventsByVenue(@PathVariable UUID venueId) {
        return eventPublicService.getEventsByVenue(venueId);
    }
}
