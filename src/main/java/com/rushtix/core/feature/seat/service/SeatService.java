package com.rushtix.core.feature.seat.service;

import com.rushtix.core.domain.entities.Event;
import com.rushtix.core.domain.entities.Seat;
import com.rushtix.core.domain.entities.TicketCategory;
import com.rushtix.core.domain.enums.SeatStatus;
import com.rushtix.core.feature.events.repository.EventRepository;
import com.rushtix.core.feature.seat.dto.SeatBulkCreateRequest;
import com.rushtix.core.feature.seat.repository.SeatRepository;
import com.rushtix.core.feature.ticketcategory.repository.TicketCategoryRepository;
import jakarta.transaction.Transactional;
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

    @Transactional
    public void bulkCreateSeats(UUID eventId, SeatBulkCreateRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        TicketCategory category = ticketCategoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        List<Seat> seatsToSave = new ArrayList<>();

        for (int i = 0; i < request.count(); i++) {
            String seatNum = String.valueOf(request.startNumber() + i);

            // 1. Skip if seat already exists (Idempotency)
            if (seatRepository.existsByEventIdAndRowLabelAndSeatNumber(eventId, request.rowLabel(), seatNum)) {
                continue;
            }

            // 2. Build Seat Entity
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

        // 3. Batch Save for performance
        seatRepository.saveAll(seatsToSave);
    }
}