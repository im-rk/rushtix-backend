package com.rushtix.core.feature.booking.api;

import com.rushtix.core.domain.entities.Event;
import com.rushtix.core.domain.entities.GroupBooking;
import com.rushtix.core.domain.entities.GroupPaymentItem;
import com.rushtix.core.domain.entities.Seat;
import com.rushtix.core.domain.entities.User;
import com.rushtix.core.domain.enums.GroupBookingStatus;
import com.rushtix.core.feature.auth.repository.UserRepository;
import com.rushtix.core.feature.booking.dto.UserTicketSummaryResponse;
import com.rushtix.core.feature.booking.dto.TicketItemDetail;
import com.rushtix.core.feature.booking.repository.BookingRepository;
import com.rushtix.core.feature.events.repository.EventRepository;
import com.rushtix.core.feature.grouppay.repository.GroupBookingRepository;
import com.rushtix.core.feature.seat.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/public/profile")
@RequiredArgsConstructor
public class UserProfileTicketController {

    private final BookingRepository bookingRepository;
    private final GroupBookingRepository groupBookingRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final SeatRepository seatRepository;

    @GetMapping("/tickets")
    public ResponseEntity<List<UserTicketSummaryResponse>> getUserTickets(@RequestHeader("X-User-Id") UUID userId) {
        List<UserTicketSummaryResponse> allTickets = new ArrayList<>();
        
        User user = userRepository.findById(userId).orElseThrow();

        // 1. Fetch Standard Single Bookings
        List<UserTicketSummaryResponse> standardBookings = bookingRepository.findAllUserBookingsWithSeats(userId)
            .stream()
            .map(b -> new UserTicketSummaryResponse(
                b.getId(),
                b.getEvent().getTitle(),
                b.getEvent().getVenue().getName(),
                b.getEvent().getEventDate(),
                
                b.getStatus().toString(),
                b.getTotalAmount(),
                b.getSeats().stream().map(s -> new TicketItemDetail(
                    s.getId(),
                    s.getRowLabel() + s.getSeatNumber(),
                    s.getQrToken()
                )).collect(Collectors.toList())
            )).collect(Collectors.toList());

        allTickets.addAll(standardBookings);

        // 2. Fetch Group Bookings
        List<GroupBooking> groupBookings = groupBookingRepository.findGroupBookingsForUser(userId, user.getEmail(), GroupBookingStatus.CONFIRMED);
        
        for (GroupBooking gb : groupBookings) {
            Event event = eventRepository.findById(gb.getEventId()).orElseThrow();
            
            boolean isInitiator = gb.getInitiatorUserId().equals(userId);
            
            // If initiator, show all items. If friend, show only their assigned items
            List<GroupPaymentItem> itemsToShow = gb.getPaymentItems().stream()
                .filter(item -> isInitiator || user.getEmail().equalsIgnoreCase(item.getFriendEmail()))
                .collect(Collectors.toList());

            if (itemsToShow.isEmpty()) continue;

            List<TicketItemDetail> ticketDetails = new ArrayList<>();
            for (GroupPaymentItem item : itemsToShow) {
                Seat seat = seatRepository.findById(item.getAssignedSeatId()).orElseThrow();
                ticketDetails.add(new TicketItemDetail(
                    seat.getId(),
                    seat.getRowLabel() + seat.getSeatNumber(),
                    seat.getQrToken()
                ));
            }

            allTickets.add(new UserTicketSummaryResponse(
                gb.getId(),
                event.getTitle(),
                event.getVenue().getName(),
                event.getEventDate(),
                "GROUP_CONFIRMED",
                isInitiator ? gb.getTotalAmount() : gb.getPerPersonAmount(),
                ticketDetails
            ));
        }

        // Sort descending by event date
        allTickets.sort((a, b) -> b.eventDate().compareTo(a.eventDate()));

        return ResponseEntity.ok(allTickets);
    }
}
