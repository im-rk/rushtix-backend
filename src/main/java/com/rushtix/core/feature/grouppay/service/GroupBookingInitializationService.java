package com.rushtix.core.feature.grouppay.service;

import com.rushtix.core.domain.entities.Booking;
import com.rushtix.core.domain.entities.GroupBooking;
import com.rushtix.core.domain.entities.GroupPaymentItem;
import com.rushtix.core.domain.entities.Seat;
import com.rushtix.core.domain.enums.BookingStatus;
import com.rushtix.core.domain.enums.GroupBookingStatus;
import com.rushtix.core.domain.enums.SeatStatus;
import com.rushtix.core.domain.enums.SplitStatus;
import com.rushtix.core.feature.booking.repository.BookingRepository;
import com.rushtix.core.feature.booking.service.PaymentGatewayService;
import com.rushtix.core.feature.grouppay.dto.InitiateGroupRequest;
import com.rushtix.core.feature.grouppay.dto.InitiateGroupResponse;
import com.rushtix.core.feature.grouppay.repository.GroupBookingRepository;
import com.rushtix.core.feature.grouppay.repository.GroupPaymentItemRepository;
import com.rushtix.core.feature.seat.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class GroupBookingInitializationService {

    private final BookingRepository bookingRepository;
    private final SeatRepository seatRepository;
    private final GroupBookingRepository groupBookingRepository;
    private final GroupPaymentItemRepository groupPaymentItemRepository;
    private final PaymentGatewayService paymentGatewayService;

    public InitiateGroupResponse convertToGroupPaySaga(InitiateGroupRequest request) {
        Booking booking= bookingRepository.findById(request.bookingId())
                .orElseThrow(()->new RuntimeException("Booking not found"));
        if(booking.getStatus()!= BookingStatus.PENDING)
        {
            throw new RuntimeException("Booking is not in PENDING status");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        int seatCount=booking.getSeats().size();
        BigDecimal perPersonAmount=booking.getTotalAmount().divide(BigDecimal.valueOf(seatCount),2, RoundingMode.HALF_UP);

        GroupBooking masterSaga=GroupBooking.builder()
                .evenetId(booking.getEvent().getId())
                .initiatorUserId(booking.getUser().getId())
                .status(GroupBookingStatus.PENDING_GROUP_PAYMENT)
                .totalAmount(booking.getTotalAmount())
                .perPersonAmount(perPersonAmount)
                .createdAt(OffsetDateTime.now())
                .expiresAt(booking.getExpiresAt())
                .build();

        GroupBooking savedSaga=groupBookingRepository.save(masterSaga);
        String initiatorUrl="";

        for(int i=0;i<seatCount;i++)
        {
            Seat seat=booking.getSeats().get(i);
            seat.setStatus(SeatStatus.GROUP_LOCKED);
            seatRepository.save(seat);

            GroupPaymentItem item=GroupPaymentItem.builder()
                    .groupBooking(savedSaga)
                    .assignedSeatId(seat.getId())
                    .status(SplitStatus.PENDING)
                    .build();
            GroupPaymentItem savedItem=groupPaymentItemRepository.save(item);

            try {
                // Call Stripe Checkout Hosted API (Passing our internal item primary key as tracking metadata)
                String checkoutUrl = paymentGatewayService.createStripeCheckoutSessionUrl(savedItem.getId(), perPersonAmount);
                savedItem.set(checkoutUrl);
                groupPaymentItemRepository.save(savedItem);

                // Assign the first link directly to the group leader
                if (i == 0) {
                    initiatorUrl = checkoutUrl;
                    savedItem.setFriendEmail(booking.getUser().getEmail());
                    groupPaymentItemRepository.save(savedItem);
                }
            } catch (Exception e) {
                throw new RuntimeException("Stripe initialization crashed during parallel checkout token generation", e);
            }
        }

        return new InitiateGroupResponse(
                savedSaga.getId(),
                initiatorUrl,
                savedSaga.getExpiresAt(),
                "Split-Pay initialization complete. Group hold locked for 10 minutes."
        );
    }

}
