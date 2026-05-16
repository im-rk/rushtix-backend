package com.rushtix.core.feature.seat.mapper;

import com.rushtix.core.domain.entities.Seat;
import com.rushtix.core.feature.seat.dto.SeatResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SeatMapper {

    @Mapping(target = "eventId", source = "event.id")
    @Mapping(target = "cateqoryId", source = "category.id")
    @Mapping(target = "lockedById", source = "lockedBy.id")
    @Mapping(target = "bookedById", source = "bookedBy.id")
    @Mapping(target = "bookingId", source = "booking.id")
    SeatResponse toResponse(Seat seat);

    List<SeatResponse> toResponseList(List<Seat> seats);
}
