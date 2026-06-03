package com.rushtix.core.feature.booking.mapper;

import com.rushtix.core.domain.entities.Booking;
import com.rushtix.core.feature.booking.dto.BookingOrganizerDetailResponse;
import com.rushtix.core.feature.booking.dto.BookingOrganizerSummaryResponse;
import com.rushtix.core.feature.booking.dto.BookingUserResponse;
import com.rushtix.core.feature.seat.mapper.SeatMapper;
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


    @Mapping(target = "bookingId", source = "id")
    @Mapping(target = "eventId", source = "event.id")
    @Mapping(target = "eventTitle", source = "event.title")
    @Mapping(target = "paymentClientSecret", constant = "mock_secret_intent_token_99824")
    @Mapping(target = "gatewayType", constant = "MOCK_GATEWAY")
    BookingUserResponse  toUserResponse(Booking booking);

    List<BookingUserResponse> toUserResponseList(List<Booking> bookings);
}
