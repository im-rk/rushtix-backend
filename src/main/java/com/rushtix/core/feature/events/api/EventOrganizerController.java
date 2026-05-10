package com.rushtix.core.feature.events.api;

import com.rushtix.core.feature.events.dto.EventDetailResponse;
import com.rushtix.core.feature.events.dto.EventRequest;
import com.rushtix.core.feature.events.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizer/events")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ORGANIZER')")
public class EventOrganizerController {

    private final EventService eventService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventDetailResponse createEvent(@RequestBody @Valid EventRequest request) {
        // TODO: Replace with SecurityUtils.getCurrentUserId() after Auth setup
        UUID organizerId = UUID.fromString("00000000-0000-0000-0000-000000000000");
        return eventService.createEvent(request, organizerId);
    }

    @GetMapping
    public List<EventDetailResponse> getAllMyEvents() {
        // TODO: Replace with SecurityUtils.getCurrentUserId() after Auth setup
        UUID organizerId = UUID.fromString("00000000-0000-0000-0000-000000000000");
        return eventService.getAllMyEvents(organizerId);
    }

    @GetMapping("/{id}")
    public EventDetailResponse getEventDetails(@PathVariable UUID id) {
        // TODO: Replace with SecurityUtils.getCurrentUserId() after Auth setup
        UUID organizerId = UUID.fromString("00000000-0000-0000-0000-000000000000");
        return eventService.getEventById(id, organizerId);
    }

    @PutMapping("/{id}")
    public EventDetailResponse updateEvent(
            @PathVariable UUID id,
            @RequestBody @Valid EventRequest request) {
        // TODO: Replace with SecurityUtils.getCurrentUserId() after Auth setup
        UUID organizerId = UUID.fromString("00000000-0000-0000-0000-000000000000");
        return eventService.updateEvent(id, organizerId, request);
    }

    @PatchMapping("/{id}/publish")
    public EventDetailResponse publishEvent(@PathVariable UUID id) {
        // TODO: Replace with SecurityUtils.getCurrentUserId() after Auth setup
        UUID organizerId = UUID.fromString("00000000-0000-0000-0000-000000000000");
        return eventService.publishEvent(id, organizerId);
    }

    @PatchMapping("/{id}/cancel")
    public EventDetailResponse cancelEvent(
            @PathVariable UUID id,
            @RequestParam(required = false) String reason) {
        // TODO: Replace with SecurityUtils.getCurrentUserId() after Auth setup
        UUID organizerId = UUID.fromString("00000000-0000-0000-0000-000000000000");
        return eventService.cancelEvent(id, organizerId, reason);
    }
    
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEvent(@PathVariable UUID id) {
        // TODO: Replace with SecurityUtils.getCurrentUserId() after Auth setup
        UUID organizerId = UUID.fromString("00000000-0000-0000-0000-000000000000");
        eventService.deleteEvent(id, organizerId);
    }
}
