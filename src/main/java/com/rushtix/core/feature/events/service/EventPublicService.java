package com.rushtix.core.feature.events.service;

import com.rushtix.core.domain.entities.Event;
import com.rushtix.core.domain.enums.EventStatus;
import com.rushtix.core.feature.events.dto.EventDetailResponse;
import com.rushtix.core.feature.events.dto.EventSummaryResponse;
import com.rushtix.core.feature.events.mapper.EventMapper;
import com.rushtix.core.feature.events.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventPublicService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    /**
     * Get all published events (for public browsing)
     * Shows only upcoming events
     *
     * @return List of event summaries
     */
    @Transactional(readOnly = true)
    public List<EventSummaryResponse> getAllUpcomingEvents() {
        return eventRepository.findAllByStatusAndEventDateAfter(
                EventStatus.PUBLISHED,
                OffsetDateTime.now()
        )
        .stream()
        .map(eventMapper::toSummaryResponse)
        .toList();
    }

    /**
     * Get single event details by ID (public endpoint)
     * Only returns PUBLISHED events
     *
     * @param id Event ID
     * @return Event details with full venue info
     */
    @Transactional(readOnly = true)
    public EventDetailResponse getEventById(UUID id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new RuntimeException("Event not available for viewing");
        }

        return eventMapper.toDetailResponse(event);
    }

    /**
     * Get all published events by status
     *
     * @param status Event status
     * @return List of event summaries
     */
    @Transactional(readOnly = true)
    public List<EventSummaryResponse> getEventsByStatus(EventStatus status) {
        return eventRepository.findAllByStatus(status)
                .stream()
                .map(eventMapper::toSummaryResponse)
                .toList();
    }

    /**
     * Get all events at a specific venue
     *
     * @param venueId ID of the venue
     * @return List of event summaries at that venue
     */
    @Transactional(readOnly = true)
    public List<EventSummaryResponse> getEventsByVenue(UUID venueId) {
        return eventRepository.findAllByVenueId(venueId)
                .stream()
                .map(eventMapper::toSummaryResponse)
                .toList();
    }
}
