package com.rushtix.core.feature.events.mapper;

import com.rushtix.core.domain.entities.Event;
import com.rushtix.core.domain.entities.TicketCategory;
import com.rushtix.core.feature.events.dto.EventDetailResponse;
import com.rushtix.core.feature.events.dto.EventRequest;
import com.rushtix.core.feature.events.dto.EventSummaryResponse;
import com.rushtix.core.feature.events.dto.PublicEventDetailsResponse;
import com.rushtix.core.feature.ticketcategory.mapper.TicketCategoryMapper;
import com.rushtix.core.feature.venue.mapper.VenueMapper;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING
        ,unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {VenueMapper.class, TicketCategoryMapper.class})
public interface EventMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "organizer", ignore = true)          // Set by Service
    @Mapping(target = "venue", ignore = true)               // Set by Service
    @Mapping(target = "status", ignore = true)              // Set to DRAFT by Service
    @Mapping(target = "seatsSold", ignore = true)           // Always 0 initially
    @Mapping(target = "seatsLocked", ignore = true)         // Always 0 initially
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "cancelledAt", ignore = true)
    @Mapping(target = "cancellationReason", ignore = true)
    Event toEntity(EventRequest request);

    @Mapping(source = "event.id", target = "id")
    @Mapping(source = "event.title", target = "title")
    @Mapping(source = "event.category", target = "category")
    @Mapping(source = "event.imageUrl", target = "imageUrl")
    @Mapping(source = "event.eventDate", target = "eventDate")
    @Mapping(source = "event.venue.name", target = "venueName")
    @Mapping(source = "event.venue.city", target = "cityName")
    EventSummaryResponse toSummaryResponse(Event event);

    @Mapping(source = "event.id", target = "id")
    @Mapping(source = "event.title", target = "title")
    @Mapping(source = "event.description", target = "description")
    @Mapping(source = "event.category", target = "category")
    @Mapping(source = "event.imageUrl", target = "imageUrl")
    @Mapping(source = "event.eventDate", target = "eventDate")
    @Mapping(source = "event.eventEndDate", target = "eventEndDate")
    @Mapping(source = "event.bookingOpensAt", target = "bookingOpensAt")
    @Mapping(source = "event.bookingClosesAt", target = "bookingClosesAt")
    @Mapping(source = "event.status", target = "status")
    @Mapping(source = "event.totalSeats", target = "totalSeats")
    @Mapping(source = "event.seatsSold", target = "seatsSold")
    @Mapping(source = "event.seatsLocked", target = "seatsLocked")
    @Mapping(expression = "java(event.getTotalSeats() - event.getSeatsSold() - event.getSeatsLocked())",
             target = "availableSeats")
    @Mapping(source = "event.venue", target = "venue")  // Uses VenueMapper to convert
    @Mapping(source = "event.createdAt", target = "createdAt")
    @Mapping(source = "event.updatedAt", target = "updatedAt")
    EventDetailResponse toDetailResponse(Event event);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "organizer", ignore = true)          // Cannot change owner
    @Mapping(target = "venue", ignore = true)               // Cannot change venue
    @Mapping(target = "status", ignore = true)              // Service controls status
    @Mapping(target = "seatsSold", ignore = true)           // Booking service controls
    @Mapping(target = "seatsLocked", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "cancelledAt", ignore = true)
    @Mapping(target = "cancellationReason", ignore = true)
    void updateEntityFromRequest(EventRequest request, @MappingTarget Event event);


    @Mapping(source = "event.id", target = "id")
    @Mapping(source = "event.title", target = "title")
    @Mapping(source = "event.description", target = "description")
    @Mapping(source = "event.category", target = "category")
    @Mapping(source = "event.imageUrl", target = "imageUrl")
    @Mapping(source = "event.eventDate", target = "eventDate")
    @Mapping(source = "event.bookingOpensAt", target = "bookingOpensAt")
    @Mapping(source = "event.bookingClosesAt", target = "bookingClosesAt")
    @Mapping(source = "event.venue.name", target = "venueName")
    @Mapping(source = "event.venue.city", target = "venueCity")
    @Mapping(source = "event.venue.addressLine", target = "addressLine")
    @Mapping(source = "categories", target = "ticketCategories")
    PublicEventDetailsResponse toPublicDetailResponse(Event event, List<TicketCategory> categories);
}
