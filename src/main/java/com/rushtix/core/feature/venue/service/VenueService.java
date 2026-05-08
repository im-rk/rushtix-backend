package com.rushtix.core.feature.venue.service;


import com.rushtix.core.domain.entities.Venue;
import com.rushtix.core.domain.enums.VenueStatus;
import com.rushtix.core.feature.venue.dto.OrganizerVenueResponse;
import com.rushtix.core.feature.venue.dto.VenueRequest;
import com.rushtix.core.feature.venue.mapper.VenueMapper;
import com.rushtix.core.feature.venue.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VenueService {
        private final VenueRepository venueRepository;
        private final VenueMapper venueMapper;

        public OrganizerVenueResponse createVenue(VenueRequest request) {
                Venue venue = venueMapper.toEntity(request);
                venue.setStatus(VenueStatus.ACTIVE);
                venue.setSeatMapConfig("{}");
                Venue savedVenue = venueRepository.save(venue);
                return venueMapper.toOrganizerVenueResponse(savedVenue);
        }
}
