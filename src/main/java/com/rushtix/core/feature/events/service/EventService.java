package com.rushtix.core.feature.events.service;

import com.rushtix.core.domain.entities.Event;
import com.rushtix.core.domain.entities.User;
import com.rushtix.core.domain.entities.Venue;
import com.rushtix.core.domain.enums.EventStatus;
import com.rushtix.core.feature.events.dto.EventDetailResponse;
import com.rushtix.core.feature.events.dto.EventRequest;
import com.rushtix.core.feature.events.mapper.EventMapper;
import com.rushtix.core.feature.events.repository.EventRepository;
import com.rushtix.core.feature.seat.repository.SeatRepository;
import com.rushtix.core.feature.ticketcategory.repository.TicketCategoryRepository;
import com.rushtix.core.feature.venue.repository.VenueRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final EntityManager entityManager;
    private final VenueRepository venueRepository;
    private final JpaRepository<User, UUID> userRepository;
    private final TicketCategoryRepository ticketCategoryRepository;
    private final SeatRepository seatRepository;

    @Transactional
    public EventDetailResponse createEvent(EventRequest request, UUID organizerId) {
        // Validate temporal logic
        validateEventDates(request);
        validateBookingWindow(request);

        // Validate that organizer exists (prevent FK constraint error)
        if (!userRepository.existsById(organizerId)) {
            throw new RuntimeException("Organizer not found");
        }

        // Validate that venue exists (prevent FK constraint error)
        if (!venueRepository.existsById(request.venueId())) {
            throw new RuntimeException("Venue not found");
        }

        // Convert DTO to entity (organizer, venue, status will be null at this point)
        Event event = eventMapper.toEntity(request);

        // Link the Organizer using EntityManager.getReference (no extra query)
        User organizer = entityManager.getReference(User.class, organizerId);
        event.setOrganizer(organizer);

        // Link the Venue using EntityManager.getReference (no extra query)
        Venue venue = entityManager.getReference(Venue.class, request.venueId());
        event.setVenue(venue);

        // Set initial values
        event.setStatus(EventStatus.DRAFT);
        event.setSeatsSold(0);
        event.setSeatsLocked(0);

        Event savedEvent = eventRepository.save(event);
        return eventMapper.toDetailResponse(savedEvent);
    }

    @Transactional(readOnly = true)
    public List<EventDetailResponse> getAllMyEvents(UUID organizerId) {
        return eventRepository.findAllByOrganizerId(organizerId)
                .stream()
                .map(eventMapper::toDetailResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public EventDetailResponse getEventById(UUID id, UUID organizerId) {
        Event event = eventRepository.findByIdAndOrganizerId(id, organizerId)
                .orElseThrow(() -> new RuntimeException("Event not found or access denied"));
        return eventMapper.toDetailResponse(event);
    }

    @Transactional
    public EventDetailResponse updateEvent(UUID id, UUID organizerId, EventRequest request) {
        Event event = eventRepository.findByIdAndOrganizerId(id, organizerId)
                .orElseThrow(() -> new RuntimeException("Event not found or access denied"));

        // Validate temporal logic on update
        validateEventDates(request);
        validateBookingWindow(request);

        // Update only allowed fields via mapper
        eventMapper.updateEntityFromRequest(request, event);

        Event updatedEvent = eventRepository.save(event);
        return eventMapper.toDetailResponse(updatedEvent);
    }

    @Transactional
    public EventDetailResponse publishEvent(UUID id, UUID organizerId) {
        Event event = eventRepository.findByIdAndOrganizerId(id, organizerId)
                .orElseThrow(() -> new RuntimeException("Event not found or access denied"));

        if (event.getStatus() != EventStatus.DRAFT) {
            throw new RuntimeException("Only DRAFT events can be published");
        }

        // Validate that ticket categories have been created
        if (ticketCategoryRepository.findAllByEventIdOrderByDisplayOrderAsc(id).isEmpty()) {
            throw new RuntimeException("Cannot publish event: You must create at least one Ticket Category first.");
        }

        // Validate that a seat map has been generated
        if (!seatRepository.existsByEventId(id)) {
            throw new RuntimeException("Cannot publish event: You must generate a Seat Map first.");
        }

        event.setStatus(EventStatus.PUBLISHED);
        Event savedEvent = eventRepository.save(event);
        return eventMapper.toDetailResponse(savedEvent);
    }

    @Transactional
    public EventDetailResponse cancelEvent(UUID id, UUID organizerId, String reason) {
        Event event = eventRepository.findByIdAndOrganizerId(id, organizerId)
                .orElseThrow(() -> new RuntimeException("Event not found or access denied"));

        // Prevent cancelling an already cancelled event
        if (event.getStatus() == EventStatus.CANCELLED) {
            throw new RuntimeException("Event is already cancelled");
        }

        event.setStatus(EventStatus.CANCELLED);
        event.setCancellationReason(reason);
        event.setCancelledAt(java.time.OffsetDateTime.now());

        Event savedEvent = eventRepository.save(event);
        return eventMapper.toDetailResponse(savedEvent);
    }

    @Transactional
    public void deleteEvent(UUID id, UUID organizerId) {
        Event event = eventRepository.findByIdAndOrganizerId(id, organizerId)
                .orElseThrow(() -> new RuntimeException("Event not found or access denied"));

        if (event.getStatus() != EventStatus.DRAFT) {
            throw new RuntimeException("Only DRAFT events can be deleted");
        }

        // Delete all associated ticket categories first to prevent foreign key constraint violations
        ticketCategoryRepository.deleteAllByEventId(id);
        
        eventRepository.delete(event);
    }

    // --- VALIDATION HELPERS ---

    private void validateEventDates(EventRequest request) {
        if (request.eventEndDate() != null &&
            request.eventEndDate().isBefore(request.eventDate())) {
            throw new RuntimeException("Event end date cannot be before event date");
        }
    }

    private void validateBookingWindow(EventRequest request) {
        if (request.bookingClosesAt().isBefore(request.bookingOpensAt())) {
            throw new RuntimeException("Booking close time cannot be before opening time");
        }
    }
}
