package com.rushtix.core.feature.seat.api;

import com.rushtix.core.feature.seat.dto.SeatBulkCreateRequest;
import com.rushtix.core.feature.seat.service.SeatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/events/{eventId}/seats")
@RequiredArgsConstructor
public class SeatAdminController {
    private final SeatService seatService;

     // Future Admin Endpoints:
     // POST /api/v1/admin/events/{eventId}/seats/bulk-create
     // PUT /api/v1/admin/events/{eventId}/seats/{seatId}
     // DELETE /api/v1/admin/events/{eventId}/seats/{seatId}

    @PostMapping("/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    public void bulkCreateSeats(@PathVariable UUID eventId, @RequestBody @Valid SeatBulkCreateRequest request) {
        seatService.bulkCreateSeats(eventId, request);}
}
