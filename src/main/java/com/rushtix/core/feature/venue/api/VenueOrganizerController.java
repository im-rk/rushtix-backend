package com.rushtix.core.feature.venue.api;

import com.rushtix.core.feature.venue.dto.OrganizerVenueResponse;
import com.rushtix.core.feature.venue.dto.VenueRequest;
import com.rushtix.core.feature.venue.service.VenueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/organizer/venues")
@RequiredArgsConstructor
public class VenueOrganizerController {

    private final VenueService venueService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrganizerVenueResponse createVenue(@RequestBody @Valid VenueRequest request) {
        return venueService.createVenue(request);
    }
}
