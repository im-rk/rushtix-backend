package com.rushtix.core.feature.seat.api;

import com.rushtix.core.feature.seat.dto.SeatBulkCreateRequest;
import com.rushtix.core.feature.seat.dto.SeatResponse;
import com.rushtix.core.feature.seat.dto.SeatUpdateRequest;
import com.rushtix.core.feature.seat.service.SeatService;
import com.rushtix.core.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizer/events/{eventId}/seats")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ORGANIZER')")
public class SeatOrganizerController {
    private final SeatService seatService;

    @PostMapping("/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    public void bulkCreateSeats(@PathVariable UUID eventId, @RequestBody @Valid SeatBulkCreateRequest request) {
        UUID organizerId = SecurityUtils.getCurrentUserId();
        seatService.bulkCreateSeats(eventId, organizerId, request);
    }

    @GetMapping
    public List<SeatResponse> getSeatMap(@PathVariable UUID eventId) {
        UUID organizerId = SecurityUtils.getCurrentUserId();
        return seatService.getSeatMapforEvent(eventId, organizerId);
    }

    @PutMapping("/{seatId}")
    public SeatResponse updateSeat(
            @PathVariable UUID eventId,
            @PathVariable UUID seatId,
            @Valid @RequestBody SeatUpdateRequest request) {
        UUID organizerId = SecurityUtils.getCurrentUserId();
        return seatService.updateSeat(eventId, organizerId, seatId, request);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearSeatMap(@PathVariable UUID eventId) {
        UUID organizerId = SecurityUtils.getCurrentUserId();
        seatService.clearSeatMap(eventId, organizerId);
    }
}
