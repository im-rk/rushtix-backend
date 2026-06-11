package com.rushtix.core.feature.booking.mapper;

import com.rushtix.core.domain.entities.Booking;
import com.rushtix.core.domain.entities.Seat;
import com.rushtix.core.domain.enums.BookingStatus;
import com.rushtix.core.feature.booking.dto.*;
import com.rushtix.core.feature.seat.mapper.SeatMapper;
import com.stripe.model.PaymentIntent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

import java.util.Collections;
import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {SeatMapper.class})
public interface BookingMapper {

    @Mapping(target = "bookingId", source = "id")
    @Mapping(target = "customerName", source = "user.fullName")
    @Mapping(target = "customerEmail", source = "user.email")
    @Mapping(target = "ticketCount", expression = "java(booking.getSeats() != null ? booking.getSeats().size() : 0)")
    BookingOrganizerSummaryResponse toOrganizerSummary(Booking booking);

    @Mapping(target = "bookingId", source = "id")
    @Mapping(target = "eventId", source = "event.id")
    @Mapping(target = "eventTitle", source = "event.title")
    @Mapping(target = "customerName", source = "user.fullName")
    @Mapping(target = "customerEmail", source = "user.email")
    BookingOrganizerDetailResponse toOrganizerDetail(Booking booking);


    @Mapping(source = "booking.id", target = "bookingId")
    @Mapping(source = "booking.event.id", target = "eventId")
    @Mapping(source = "booking.event.title", target = "eventTitle")
    @Mapping(source = "intent.clientSecret", target = "paymentClientSecret")
    @Mapping(source = "booking.status", target = "status")
    @Mapping(constant = "STRIPE", target = "gatewayType")
    BookingUserResponse toUserResponse(Booking booking, PaymentIntent intent);

    @Mapping(source = "booking.id", target = "bookingId")
    @Mapping(source = "booking.event.id", target = "eventId")
    @Mapping(source = "booking.event.title", target = "eventTitle")
    @Mapping(constant = "", target = "paymentClientSecret") // Handshake is over, secret is no longer needed
    @Mapping(constant = "STRIPE", target = "gatewayType")
    BookingUserResponse toUserResponse(Booking booking);

    List<BookingUserResponse> toUserResponseList(List<Booking> bookings);


    @Mapping(source = "booking.event.title",target = "eventTitle")
    @Mapping(source = "booking.event.eventDate",target = "eventDateTime")
    @Mapping(source = "booking.event.venue.name",target = "venueName")
    @Mapping(source = "booking.event.venue.city",target = "venueCity")
    @Mapping(target = "status",expression = "java(booking.getStatus())")
    @Mapping(source = "booking",target = "tickets", qualifiedByName = "seatsToTicketPasses")
    BookingStatusResponse toStatusResponse(Booking booking);

    @Named("seatsToTicketPasses")
    default List<TicketPassResponse> seatsToTicketPasses(Booking booking) {
        if (booking.getStatus() != BookingStatus.CONFIRMED || booking.getSeats()==null) {
            return Collections.emptyList();
        }
        return booking.getSeats().stream()
                .map(this::toTicketPassResponse)
                .toList();
    }

    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(source = "displayLabel",target = "seatLabel")
    @Mapping(source = "qrToken",target = "qrtoken")
    TicketPassResponse toTicketPassResponse(Seat seat);
}
