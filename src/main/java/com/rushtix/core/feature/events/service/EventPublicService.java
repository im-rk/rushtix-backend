package com.rushtix.core.feature.events.service;

import com.rushtix.core.domain.entities.Event;
import com.rushtix.core.domain.enums.EventStatus;
import com.rushtix.core.feature.events.dto.EventDetailResponse;
import com.rushtix.core.feature.events.dto.EventSummaryResponse;
import com.rushtix.core.feature.events.dto.PublicEventDetailsResponse;
import com.rushtix.core.feature.events.mapper.EventMapper;
import com.rushtix.core.feature.events.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class EventPublicService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    @Transactional(readOnly = true)
    public List<EventSummaryResponse> getPublicActiveEvents(String category,String city, String state, UUID venueId)
    {
        List<Event> activeEvents;
        OffsetDateTime now = OffsetDateTime.now();
        if(city!=null && !city.isBlank())
        {
            activeEvents=eventRepository.findAllByStatusAndEventDateAfterAndVenueStateIgnoreCase(
                    EventStatus.PUBLISHED,now,city.trim()
            );
        }
        else if(state!=null && !state.isBlank())
        {
            activeEvents=eventRepository.findAllByStatusAndEventDateAfterAndVenueCityIgnoreCase(
                    EventStatus.PUBLISHED,now,state.trim()
            );
        }
        else
        {
            activeEvents=eventRepository.findAllByStatusAndEventDateAfter(
                    EventStatus.PUBLISHED,now
            );
        }

        return activeEvents.stream()
                .filter(e->category==null || e.getCategory().equalsIgnoreCase(category))
                .filter(e->venueId==null || e.getVenue().getId().equals(venueId))
                .map(eventMapper::toSummaryResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PublicEventDetailsResponse getPublicEventDetails(UUID eventId)
    {
        Event event=eventRepository.findById(eventId)
                .orElseThrow(()->new RuntimeException("Event not found with id: "+eventId));
        if(event.getStatus()!=EventStatus.PUBLISHED || event.getEventDate().isBefore(OffsetDateTime.now()))
        {
            throw new RuntimeException("Event is not available for public view");
        }
        return
    }


}
