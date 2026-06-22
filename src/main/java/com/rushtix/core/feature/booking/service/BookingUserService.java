package com.rushtix.core.feature.booking.service;

import com.rushtix.core.domain.entities.*;
import com.rushtix.core.domain.enums.BookingStatus;
import com.rushtix.core.domain.enums.PaymentStatus;
import com.rushtix.core.domain.enums.SeatStatus;
import com.rushtix.core.feature.booking.dto.BookingRequst;
import com.rushtix.core.feature.booking.dto.BookingReservationResponse;
import com.rushtix.core.feature.booking.dto.BookingStatusResponse;
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
    public List<Seat> verifyAndHoldInventory(List<UUID> seatIds, User userContext, OffsetDateTime expiration) {
        boolean locksAcquired = redisLockService.acquireSeatLocks(seatIds, userContext.getId(), 10);
        if (!locksAcquired) {
            throw new RuntimeException("Unable to acquire locks on selected seats. Please try again.");
        }

        try {
            List<Seat> seats = seatRepository.findAndLockSeatsByIds(seatIds);
            if (seats.size() != seatIds.size()) {
                throw new RuntimeException("Selected seats count mismatch or inventory mismatch");
            }

            for (Seat seat : seats) {
                boolean isLockStale = seat.getStatus() == SeatStatus.LOCKED &&
                        seat.getLockedUntil() != null &&
                        seat.getLockedUntil().isBefore(OffsetDateTime.now());

                if (seat.getStatus() != SeatStatus.AVAILABLE && !isLockStale) {
                    throw new RuntimeException("Seat " + seat.getDisplayLabel() + " is already occupied or pending purchase");
                }

                TicketCategory category = seat.getCategory();
                seat.setPricePaid(category.getCurrentPrice());
                seat.setPriceMultiplier(BigDecimal.ONE);
                seat.setStatus(SeatStatus.LOCKED);
                seat.setLockedUntil(expiration);
                seat.setLockedBy(userContext);
            }
            return seatRepository.saveAll(seats);
        } catch (Exception e) {
            redisLockService.releaseSeatLocks(seatIds, userContext.getId());
            throw e;
        }
    }

    @Transactional
    public BookingReservationResponse createBookingReservation(BookingRequst request, User userContext) {
        OffsetDateTime windowExpiration = OffsetDateTime.now().plusMinutes(10);

        // REUSE THE SHARED LOCK ENGINE HERE
        List<Seat> seats = verifyAndHoldInventory(request.seatIds(), userContext, windowExpiration);

        Event event = eventRepository.findById(request.eventId())
                .orElseThrow(() -> new RuntimeException("Event layout verification failed"));

        BigDecimal calculatedTotal = seats.stream()
                .map(Seat::getPricePaid)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Persist transaction container
        Booking booking = Booking.builder()
                .user(userContext)
                .event(event)
                .status(BookingStatus.PENDING)
                .totalAmount(calculatedTotal)
                .idempotencyKey(request.idempotencyKey())
                .expiresAt(windowExpiration)
                .build();

        for (Seat seat : seats) {
            seat.setBooking(booking);
            booking.getSeats().add(seat);
        }
        Booking savedBooking = bookingRepository.save(booking);

        return bookingMapper.toReservationResponse(savedBooking);
    }

    @Transactional
    public BookingUserResponse initiateSinglePaymentExecution(UUID bookingId, String idempotencyKey) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking tracking context reference not found"));

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("Booking is not in a payable pending state");
        }

        // Generate the single 100% full amount payment intent via Stripe here
        PaymentIntent intent = paymentGatewayService.createPaymentIntent(booking.getId(), booking.getTotalAmount());

        Payment paymentLedger = Payment.builder()
                .booking(booking)
                .provider("STRIPE")
                .providerPaymentId(intent.getId())
                .amount(booking.getTotalAmount())
                .currency("INR")
                .status(PaymentStatus.PENDING)
                .idempotencyKey(idempotencyKey)
                .providerMetadata("{}")
                .build();
        paymentRepository.save(paymentLedger);

        return bookingMapper.toUserResponse(booking, intent);
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
        Payment payment = paymentRepository.findByProviderPaymentId(providerPaymentId)
                .orElseThrow(() -> new RuntimeException("Payment tracking ledger item row corrupted or missing"));

        // Secure state transition executions
        payment.setStatus(PaymentStatus.SUCCEEDED);
        payment.setProviderMetadata(rawJsonMetadata);
        payment.setCompletedAt(OffsetDateTime.now());
        paymentRepository.save(payment);

        // 2. Finalize master ticket state records
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setConfirmedAt(OffsetDateTime.now());

        for (Seat seat : booking.getSeats()) {
            seat.setStatus(SeatStatus.BOOKED);
            seat.setBookedBy(booking.getUser());
            seat.setQrToken("RUSH-TIX-" + UUID.randomUUID().toString().replace("-", "").toUpperCase());
        }

        Booking savedBooking = bookingRepository.save(booking);

        redisLockService.releaseSeatLocks(seatIds, booking.getUser().getId());

        return bookingMapper.toUserResponse(savedBooking);
    }

    @Transactional(readOnly = true)
    public BookingStatusResponse getFulfillmentStatus(UUID bookingId,UUID authenticatedUserId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking transaction not found"));

        if (!booking.getUser().getId().equals(authenticatedUserId)) {
            throw new RuntimeException("Unauthorized access to booking status");
        }
        return bookingMapper.toStatusResponse(booking);
    }
}
