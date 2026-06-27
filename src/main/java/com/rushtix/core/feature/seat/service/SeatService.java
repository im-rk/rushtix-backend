package com.rushtix.core.feature.seat.service;

import com.rushtix.core.domain.entities.Event;
import com.rushtix.core.domain.entities.Seat;
import com.rushtix.core.domain.entities.TicketCategory;
import com.rushtix.core.domain.enums.SeatStatus;
import com.rushtix.core.feature.events.repository.EventRepository;
import com.rushtix.core.feature.seat.dto.SeatBulkCreateRequest;
import com.rushtix.core.feature.seat.dto.SeatResponse;
import com.rushtix.core.feature.seat.dto.SeatUpdateRequest;
import com.rushtix.core.feature.seat.mapper.SeatMapper;
import com.rushtix.core.feature.seat.repository.SeatRepository;
import com.rushtix.core.feature.ticketcategory.repository.TicketCategoryRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SeatService {

    private final SeatRepository seatRepository;
    private final EventRepository eventRepository;
    private final TicketCategoryRepository ticketCategoryRepository;
    private final SeatMapper seatMapper;

    @Transactional
    public void bulkCreateSeats(UUID eventId, UUID organizerId, SeatBulkCreateRequest request) {
        Event event = eventRepository.findByIdAndOrganizerId(eventId, organizerId)
                .orElseThrow(() -> new RuntimeException("Event not found or access denied"));

        TicketCategory category = ticketCategoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        if (!category.getEvent().getId().equals(eventId)) {
            throw new RuntimeException("Category does not belong to event " + eventId);
        }

        int currentAllocated = seatRepository.countByCategoryId(category.getId());
        if (currentAllocated + request.count() > category.getCapacity()) {
            throw new RuntimeException(String.format("Cannot generate %d seats. Category '%s' only has %d unallocated seats remaining (Capacity: %d).", 
                request.count(), category.getName(), category.getCapacity() - currentAllocated, category.getCapacity()));
        }

        List<Seat> seatsToSave = new ArrayList<>();

        for (int i = 0; i < request.count(); i++) {
            String seatNum = String.valueOf(request.startNumber() + i);

            // Build Seat Entity
            Seat seat = Seat.builder()
                    .event(event)
                    .category(category)
                    .rowLabel(request.rowLabel())
                    .seatNumber(seatNum)
                    .displayLabel(request.rowLabel() + "-" + seatNum)
                    .isAccessible(request.isAccessible())
                    .status(SeatStatus.AVAILABLE)
                    .build();

            seatsToSave.add(seat);
        }

        // Batch Save with constraint violation handling
        try {
            seatRepository.saveAllAndFlush(seatsToSave);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Some seats already exist for this event and row: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<SeatResponse> getSeatMapforEvent(UUID eventId, UUID organizerId) {
        // 1. Validate Event Exists and belongs to organizer
        eventRepository.findByIdAndOrganizerId(eventId, organizerId)
                .orElseThrow(() -> new RuntimeException("Event not found or access denied"));

        // 2. Fetch Seats and Map to Response DTOs
        List<Seat> seats = seatRepository.findAllByEventIdOrderByRowLabelAscSeatNumberAsc(eventId);
        return seatMapper.toResponseList(seats);
    }

    @Transactional
    public SeatResponse updateSeat(UUID eventId, UUID organizerId, UUID seatId, SeatUpdateRequest request) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new RuntimeException("Seat not found"));

        if (!seat.getEvent().getId().equals(eventId)) {
            throw new RuntimeException("Seat does not belong to event " + eventId);
        }

        if (!seat.getEvent().getOrganizer().getId().equals(organizerId)) {
            throw new RuntimeException("Access denied: you do not own this event");
        }

        if(seat.getStatus()==SeatStatus.BOOKED || seat.getStatus()==SeatStatus.LOCKED){
            throw new RuntimeException("Cannot update a seat that is currently locked or sold");
        }

        boolean duplicateExists=seatRepository.existsByEventIdAndRowLabelAndSeatNumberAndIdNot(
                seat.getEvent().getId(),
                request.rowLabel(),
                request.seatNumber(),
                seatId
        );
        if(duplicateExists){
            throw new RuntimeException("Another seat with the same row and number already exists in this event");
        }


        seat.setRowLabel(request.rowLabel());
        seat.setSeatNumber(request.seatNumber());
        seat.setDisplayLabel(request.rowLabel()+ "-" + request.seatNumber());
        seat.setAccessible(request.isAccessible());
        seat.setStatus(request.status());

        Seat updatedSeat = seatRepository.save(seat);
        return seatMapper.toResponse(updatedSeat);
    }

    @Transactional
    public void clearSeatMap(UUID eventId, UUID organizerId) {
        // 1. Validate Event Exists and belongs to organizer
        eventRepository.findByIdAndOrganizerId(eventId, organizerId)
                .orElseThrow(() -> new RuntimeException("Event not found or access denied"));

        // 2. Check for Active Commitments
        boolean hasActiveCommitments = seatRepository.existsByEventIdAndStatusIn(
                eventId,
                List.of(SeatStatus.LOCKED, SeatStatus.BOOKED)
        );
        if (hasActiveCommitments) {
            throw new RuntimeException("Cannot clear seat map with active locked or sold seats");
        }

        // 3. Perform Hard Delete
        seatRepository.deleteAllByEventId(eventId);
    }
}