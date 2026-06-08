package com.rushtix.core.feature.booking.service;

import com.rushtix.core.domain.entities.*;
import com.rushtix.core.domain.enums.BookingStatus;
import com.rushtix.core.domain.enums.PaymentStatus;
import com.rushtix.core.domain.enums.SeatStatus;
import com.rushtix.core.feature.booking.dto.BookingRequst;
import com.rushtix.core.feature.booking.dto.BookingUserResponse;
import com.rushtix.core.feature.booking.mapper.BookingMapper;
import com.rushtix.core.feature.booking.repository.BookingRepository;
import com.rushtix.core.feature.booking.repository.PaymentRepository;
import com.rushtix.core.feature.events.repository.EventRepository;
import com.rushtix.core.feature.seat.repository.SeatRepository;
import com.stripe.model.PaymentIntent;
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
    private final PaymentGatewayService paymentGatewayService;
    private final PaymentRepository paymentRepository;

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
            Booking savedBooking = bookingRepository.save(booking);

            PaymentIntent intent=paymentGatewayService.createPaymentIntent(savedBooking.getId(),calculatedTotal);
            Payment paymentLedger=Payment.builder()
                    .booking(savedBooking)
                    .provider("STRIPE")
                    .providerPaymentId(intent.getId())
                    .amount(calculatedTotal)
                    .currency("INR")
                    .status(PaymentStatus.PENDING)
                    .idempotencyKey(request.idempotencyKey())
                    .providerMetadata("{}")
                    .build();
            paymentRepository.save(paymentLedger);

            return bookingMapper.toUserResponse(savedBooking,intent);
        }
        catch(Exception e)
        {
            redisLockService.releaseSeatLocks(request.seatIds(),userContext.getId());
            throw e;
        }

    }

    @Transactional
    public BookingUserResponse confirmBookingPayment(UUID bookingId, String providerPaymentId, String rawJsonMetadata) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking tracking context reference not found"));

        List<UUID> seatIds = booking.getSeats().stream().map(Seat::getId).toList();

        // Safety Guard: Avoid re-processing if this method was already executed by a fast-tracked thread pipeline
        if (booking.getStatus() != BookingStatus.PENDING) {
            return bookingMapper.toUserResponse(booking);
        }

        // 1. Locate and finalize underlying transaction logging parameters
        Payment payment = paymentRepository.findbyProviderPaymentId(providerPaymentId)
                .orElseThrow(() -> new RuntimeException("Payment tracking ledger item row corrupted or missing"));

        // Secure state transition executions
        payment.setStatus(PaymentStatus.SUCCEEDED);
        payment.setProviderMetadata(rawJsonMetadata); // Dumps the complete raw string JSON payload directly into Postgres JSONB
        payment.setCompletedAt(OffsetDateTime.now());
        paymentRepository.save(payment);

        // 2. Finalize master ticket state records
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setConfirmedAt(OffsetDateTime.now());

        for (Seat seat : booking.getSeats()) {
            seat.setStatus(SeatStatus.BOOKED);
            seat.setBookedBy(booking.getUser());
            // Generates completely unique validation tokens per ticket row instance
            seat.setQrToken("RUSH-TIX-" + UUID.randomUUID().toString().replace("-", "").toUpperCase());
        }

        Booking savedBooking = bookingRepository.save(booking);

        // 3. Housekeeping: Wipe high-speed tracking locks safely out of Redis RAM container using your verified owner footprint
        redisLockService.releaseSeatLocks(seatIds, booking.getUser().getId());

        // Compiles perfectly now using our overloaded single-parameter mapping strategy layout
        return bookingMapper.toUserResponse(savedBooking);
    }

    @Transactional(readOnly = true)
    public List<BookingUserResponse> getUserPurchaseHistory(UUID userId) {
        return bookingMapper.toUserResponseList(bookingRepository.findAllByUserIdOrderByCreatedAtDesc(userId));
    }
}
