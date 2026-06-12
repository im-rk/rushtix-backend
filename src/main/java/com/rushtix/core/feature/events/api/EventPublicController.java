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
@RequestMapping("/api/v1/public/events")
@RequiredArgsConstructor
public class EventPublicController {

    private final EventPublicService eventPublicService;

    public List<EventSummaryResponse> discoverEvents(
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "city", required = false) String city,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "venueId", required = false) UUID venueId) {
        return eventPublicService.getPublicActiveEvents(category, city, state, venueId);
    }

}
