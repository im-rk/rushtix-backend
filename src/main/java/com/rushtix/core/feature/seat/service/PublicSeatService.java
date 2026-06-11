package com.rushtix.core.feature.seat.service;

import com.rushtix.core.domain.entities.Seat;
import com.rushtix.core.feature.seat.dto.PublicSeatResponse;
import com.rushtix.core.feature.seat.mapper.SeatMapper;
import com.rushtix.core.feature.seat.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PublicSeatService {

    private final SeatRepository seatRepository;
    private final SeatMapper seatMapper;

    @Transactional(readOnly = true)
    public List<PublicSeatResponse> getPublicSeatLayoutMap(UUID eventId) {
        List<Seat> seats=seatRepository.findAllByEventIdOrderByRowLabelAscSeatNumberAsc(eventId);
        return seatMapper.toPublicResponseList(seats);
    }
}
