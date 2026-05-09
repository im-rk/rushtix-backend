package com.rushtix.core.feature.venue.api;

import com.rushtix.core.feature.venue.dto.OrganizerVenueResponse;
import com.rushtix.core.feature.venue.dto.SeatMapUpdateRequest;
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
        // TODO: Replace with SecurityUtils.getCurrentUserId() after Auth setup
        UUID organizerId = UUID.fromString("00000000-0000-0000-0000-000000000000");
        return venueService.createVenue(request, organizerId);
    }

    @GetMapping
    public List<OrganizerVenueResponse> getAllMyVenues() {
        // TODO: Replace with SecurityUtils.getCurrentUserId() after Auth setup
        UUID organizerId = UUID.fromString("00000000-0000-0000-0000-000000000000");
        return venueService.getAllMyVenues(organizerId);
    }

    @GetMapping("/{id}")
    public OrganizerVenueResponse getVenueDetails(@PathVariable UUID id)
    {
        UUID currentOrganizerId = UUID.fromString("00000000-0000-0000-0000-000000000000");
        return venueService.getVenueById(id, currentOrganizerId);
    }

    @PutMapping("/{id}")
    public OrganizerVenueResponse updateVenue(@PathVariable UUID id, @RequestBody @Valid VenueRequest request)
    {
        // TODO: Replace with SecurityUtils.getCurrentUserId() after Auth setup
        UUID currentOrganizerId = UUID.fromString("00000000-0000-0000-0000-000000000000");
        return venueService.updateVenue(id, currentOrganizerId, request);
    }

    @PatchMapping("/{id}/seatmap")
    @ResponseStatus(HttpStatus.NO_CONTENT) // Returns 204 because there's no data to return
    public void updateSeatMap(
            @PathVariable UUID id,
            @RequestBody @Valid SeatMapUpdateRequest request) {

        // TODO: Replace with SecurityUtils.getCurrentUserId() after Auth setup
        UUID currentOrganizerId = UUID.fromString("00000000-0000-0000-0000-000000000000");
        venueService.updateSeatMap(id, currentOrganizerId, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT) // This returns the 204 Status Code
    public void deleteVenue(@PathVariable UUID id) {
        // Get the ID from the Security Token (currently using a placeholder)
        UUID currentOrganizerId = UUID.fromString("00000000-0000-0000-0000-000000000000");
        venueService.deleteVenue(id, currentOrganizerId);
    }

}
