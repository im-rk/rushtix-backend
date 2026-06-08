package com.rushtix.core.feature.booking.mapper;

import com.rushtix.core.domain.entities.Booking;
import com.rushtix.core.feature.booking.dto.BookingOrganizerDetailResponse;
import com.rushtix.core.feature.booking.dto.BookingOrganizerSummaryResponse;
import com.rushtix.core.feature.booking.dto.BookingUserResponse;
import com.rushtix.core.feature.seat.mapper.SeatMapper;
import com.stripe.model.PaymentIntent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

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
    @Mapping(source = "intent.clientSecret", target = "clientSecret") // ◄ MAP STRIPE LIVE!
    @Mapping(constant = "STRIPE", target = "provider") // Hardcodes the string value
    BookingUserResponse toUserResponse(Booking booking, PaymentIntent intent);

    @Mapping(source = "booking.id", target = "bookingId")
    @Mapping(source = "booking.event.id", target = "eventId")
    @Mapping(source = "booking.event.title", target = "eventTitle")
    @Mapping(constant = "", target = "clientSecret") // Handshake is over, secret is no longer needed
    @Mapping(constant = "STRIPE", target = "provider")
    BookingUserResponse toUserResponse(Booking booking);

    List<BookingUserResponse> toUserResponseList(List<Booking> bookings);
}
