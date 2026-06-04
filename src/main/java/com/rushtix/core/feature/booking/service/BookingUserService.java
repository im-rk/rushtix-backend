package com.rushtix.core.feature.booking.service;

import com.rushtix.core.domain.entities.*;
import com.rushtix.core.domain.enums.BookingStatus;
import com.rushtix.core.domain.enums.SeatStatus;
import com.rushtix.core.feature.booking.dto.BookingRequst;
import com.rushtix.core.feature.booking.dto.BookingUserResponse;
import com.rushtix.core.feature.booking.mapper.BookingMapper;
import com.rushtix.core.feature.booking.repository.BookingRepository;
import com.rushtix.core.feature.events.repository.EventRepository;
import com.rushtix.core.feature.seat.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingUserService {

    private final BookingRepository bookingRepository;
    private final SeatRepository seatRepository;
    private final BookingMapper bookingMapper;
    private final EventRepository eventRepository;
    private final RedisLockService redisLockService;

    @Transactional
    public BookingUserResponse createBookingReservation(BookingRequst request, User userContext) {

        boolean locksAcquired=redisLockService.acquireSeatLocks(request.seatIds(),userContext.getId(),10);
        if(!locksAcquired) {
            throw new RuntimeException("Unable to acquire locks on selected seats. Please try again.");
        }

        try
        {
            Event event = eventRepository.findById(request.eventId())
                    .orElseThrow(() -> new RuntimeException("Event layout verification failed"));

            // 1. Acquire Database Lock on seats to isolate threads
            List<Seat> seats = seatRepository.findAndLockSeatsByIds(request.seatIds());

            if (seats.size() != request.seatIds().size()) {
                throw new RuntimeException("Selected seats count mismatch or inventory mismatch");
            }

            BigDecimal calculatedTotal = BigDecimal.ZERO;
            OffsetDateTime windowExpiration = OffsetDateTime.now().plusMinutes(10);

            // 2. Evaluate booking state safety boundary conditions
            for (Seat seat : seats) {
                boolean isLockStale = seat.getStatus() == SeatStatus.LOCKED &&
                        seat.getLockedUntil() != null &&
                        seat.getLockedUntil().isBefore(OffsetDateTime.now());

                if (seat.getStatus() != SeatStatus.AVAILABLE && !isLockStale) {
                    throw new RuntimeException("Seat " + seat.getDisplayLabel() + " is already occupied or pending purchase");
                }

                TicketCategory category = seat.getCategory();

                // Snapshot dynamic pricing parameters directly onto the seat item row
                seat.setPricePaid(category.getCurrentPrice());
                seat.setPriceMultiplier(BigDecimal.ONE); // For base snapshot
                seat.setStatus(SeatStatus.LOCKED);
                seat.setLockedUntil(windowExpiration);
                seat.setLockedBy(userContext);

                calculatedTotal = calculatedTotal.add(category.getCurrentPrice());
            }

            // 3. Persist transaction container
            Booking booking = Booking.builder()
                    .user(userContext)
                    .event(event)
                    .status(BookingStatus.PENDING)
                    .totalAmount(calculatedTotal)
                    .idempotencyKey(request.idempotencyKey())
                    .expiresAt(windowExpiration)
                    .build();

            // Crosslink entities
            for (Seat seat : seats) {
                seat.setBooking(booking);
                booking.getSeats().add(seat);
            }

            return bookingMapper.toUserResponse(bookingRepository.save(booking));
        }
        catch(Exception e)
        {
            redisLockService.releaseSeatLocks(request.seatIds(),userContext.getId());
            throw e;
        }

    }

    @Transactional
    public BookingUserResponse confirmBookingPayment(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking reference not found"));

        List<UUID> seatIds = booking.getSeats().stream().map(Seat::getId).toList();

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new RuntimeException("Transaction cannot be processed from state: " + booking.getStatus());
        }

        // Handle expired locks gracefully
        if (booking.getExpiresAt().isBefore(OffsetDateTime.now())) {
            booking.setStatus(BookingStatus.CANCELLED);
            booking.setCancellationReason("Checkout countdown expired");

            for (Seat seat : booking.getSeats()) {
                seat.setStatus(SeatStatus.AVAILABLE);
                seat.setBooking(null);
                seat.setLockedBy(null);
                seat.setLockedUntil(null);
            }
            bookingRepository.save(booking);

            // Release the high-speed Redis lock since time ran out
            redisLockService.releaseSeatLocks(seatIds,booking.getUser().getId());
            throw new RuntimeException("The 10-minute checkout period expired. Your seats have been released.");
        }

        // Finalize tickets securely
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setConfirmedAt(OffsetDateTime.now());

        for (Seat seat : booking.getSeats()) {
            seat.setStatus(SeatStatus.BOOKED);
            seat.setBookedBy(booking.getUser());
            seat.setQrToken("RUSH-TIX-" + UUID.randomUUID().toString().replace("-", "").toUpperCase());
        }

        Booking savedBooking = bookingRepository.save(booking);

        // SUCCESSFUL PURCHASE: Wipe the temporary lock key from Redis cleanly
        redisLockService.releaseSeatLocks(seatIds,booking.getUser().getId());

        return bookingMapper.toUserResponse(savedBooking);
    }

    @Transactional(readOnly = true)
    public List<BookingUserResponse> getUserPurchaseHistory(UUID userId) {
        return bookingMapper.toUserResponseList(bookingRepository.findAllByUserIdOrderByCreatedAtDesc(userId));
    }
}
