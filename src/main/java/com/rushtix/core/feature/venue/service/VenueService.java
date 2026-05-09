package com.rushtix.core.feature.venue.service;


import com.rushtix.core.domain.entities.User;
import com.rushtix.core.domain.entities.Venue;
import com.rushtix.core.domain.enums.VenueStatus;
import com.rushtix.core.feature.venue.dto.OrganizerVenueResponse;
import com.rushtix.core.feature.venue.dto.SeatMapUpdateRequest;
import com.rushtix.core.feature.venue.dto.VenueRequest;
import com.rushtix.core.feature.venue.mapper.VenueMapper;
import com.rushtix.core.feature.venue.repository.VenueRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VenueService {
        private final VenueRepository venueRepository;
        private final VenueMapper venueMapper;
        private final EntityManager entityManager;

        @Transactional
        public OrganizerVenueResponse createVenue(VenueRequest request, UUID organizerId) {
                Venue venue = venueMapper.toEntity(request);

                // Link the Organizer (Owner) using EntityManager.getReference to avoid extra DB query
                User organizer = entityManager.getReference(User.class, organizerId);
                venue.setOrganizer(organizer);

                // Set defaults
                venue.setStatus(VenueStatus.ACTIVE);
                venue.setSeatMapConfig("{}");

                Venue savedVenue = venueRepository.save(venue);
                return venueMapper.toOrganizerVenueResponse(savedVenue);
        }

        @Transactional(readOnly = true)
        public List<OrganizerVenueResponse> getAllMyVenues(UUID organizerId)
        {
            return venueRepository.findAllByOrganizerId(organizerId).stream().map(venueMapper::toOrganizerVenueResponse).toList();
        }

        @Transactional(readOnly = true)
        public OrganizerVenueResponse getVenueById(UUID Id,UUID organizerId)
        {
            Venue venue=venueRepository.findByIdAndOrganizerId(Id,organizerId)
                    .orElseThrow(()->new RuntimeException("venue not found or access denied"));


            return venueMapper.toOrganizerVenueResponse(venue);
        }

        @Transactional
        public OrganizerVenueResponse updateVenue(UUID id,UUID organizerID,VenueRequest request)
        {
            Venue venue=venueRepository.findByIdAndOrganizerId(id,organizerID)
                    .orElseThrow(()->new RuntimeException("Venue not found or unauthorized entry"));

            venueMapper.updateEntityFromRequest(request,venue);
            return venueMapper.toOrganizerVenueResponse(venueRepository.save(venue));
        }

        @Transactional
        public void updateSeatMap(UUID id, UUID organizerId, SeatMapUpdateRequest request)
        {
            Venue venue=venueRepository.findByIdAndOrganizerId(id,organizerId)
                    .orElseThrow(()->new RuntimeException("Venue not found or unauthorized entry"));
            venue.setSeatMapConfig(request.seatMapConfig());
            venueRepository.save(venue);
        }

        @Transactional
        public void deleteVenue(UUID id,UUID organizerId)
        {
            Venue venue=venueRepository.findByIdAndOrganizerId(id,organizerId)
                    .orElseThrow(()->new RuntimeException("Venue not found or unauthorized entry"));

            venueRepository.delete(venue);

        }
}
