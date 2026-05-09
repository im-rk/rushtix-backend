package com.rushtix.core.feature.venue.api;

import com.rushtix.core.feature.venue.dto.OrganizerVenueResponse;
import com.rushtix.core.feature.venue.dto.VenueRequest;
import com.rushtix.core.feature.venue.service.VenueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizer/venues")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ORGANIZER')")
public class VenueOrganizerController {

    private final VenueService venueService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrganizerVenueResponse createVenue(@RequestBody @Valid VenueRequest request) {
        return venueService.createVenue(request);
    }

    @GetMapping
    public List<OrganizerVenueResponse> getAllMyVenues(@PathVariable UUID organizerId) {
        return venueService.getAllMyVenues(organizerId);
    }

    @GetMapping("/{id}")
    public OrganizerVenueResponse getVenueDetails(@PathVariable UUID id)
    {
        UUID currentOrganizerId = UUID.fromString("00000000-0000-0000-0000-000000000000");
        return venueService.getVenueById(id, currentOrganizerId);
    }



}
