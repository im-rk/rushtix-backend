package com.rushtix.core.feature.seat.api;

import com.rushtix.core.feature.seat.dto.PublicSeatResponse;
import com.rushtix.core.feature.seat.service.PublicSeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/public/event/{eventId}/seats")
@RequiredArgsConstructor
public class SeatPublicController {
    private final PublicSeatService publicSeatService;

    @GetMapping
    public List<PublicSeatResponse> getPublicSeatLayoutMap(@PathVariable UUID eventId) {
        return publicSeatService.getPublicSeatLayoutMap(eventId);
    }
}
