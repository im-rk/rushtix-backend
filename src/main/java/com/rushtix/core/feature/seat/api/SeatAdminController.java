package com.rushtix.core.feature.seat.api;

import com.rushtix.core.feature.seat.dto.SeatBulkCreateRequest;
import com.rushtix.core.feature.seat.dto.SeatResponse;
import com.rushtix.core.feature.seat.dto.SeatUpdateRequest;
import com.rushtix.core.feature.seat.service.SeatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/events/{eventId}/seats")
@RequiredArgsConstructor
public class SeatAdminController {
    private final SeatService seatService;

    @PostMapping("/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    public void bulkCreateSeats(@PathVariable UUID eventId, @RequestBody @Valid SeatBulkCreateRequest request) {
        seatService.bulkCreateSeats(eventId, request);
    }

    @GetMapping
    public List<SeatResponse> getSeatMap(@PathVariable UUID eventId) {
        return seatService.getSeatMapforEvent(eventId);
    }

    @PutMapping("/{seatId}")
    public SeatResponse updateSeat(
            @PathVariable UUID seatId,
            @Valid @RequestBody SeatUpdateRequest request) {
        return seatService.updateSeat(seatId, request);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearSeatMap(@PathVariable UUID eventId) {
        seatService.clearSeatMap(eventId);
    }
}
