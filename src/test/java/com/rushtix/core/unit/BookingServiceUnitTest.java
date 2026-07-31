package com.rushtix.core.unit;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.rushtix.core.domain.entities.User;
import com.rushtix.core.feature.booking.service.PaymentGatewayService;
import com.rushtix.core.feature.booking.service.RedisLockService;
import com.rushtix.core.feature.seat.repository.SeatRepository;
import com.stripe.model.PaymentIntent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rushtix.core.domain.entities.Booking;
import com.rushtix.core.domain.enums.BookingStatus;
import com.rushtix.core.feature.booking.mapper.BookingMapper;
import com.rushtix.core.feature.booking.repository.BookingRepository;
import com.rushtix.core.feature.booking.repository.PaymentRepository;
import com.rushtix.core.feature.booking.service.BookingUserService;

@ExtendWith(MockitoExtension.class)
public class BookingServiceUnitTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private BookingMapper bookingMapper;
    @Mock private RedisLockService redisLockService;
    @Mock private PaymentGatewayService paymentGatewayService;
    @Mock private SeatRepository seatRepository;

    @InjectMocks private BookingUserService bookingUserService;

    @Test
    public void confirmBookingPayment_WhenBookingNotPending_ShouldReturnEarlyWithoutSaving() {

        UUID id = UUID.randomUUID();
        Booking b = new Booking();
        b.setId(id);
        b.setStatus(BookingStatus.CONFIRMED);
        when(bookingRepository.findById(id)).thenReturn(Optional.of(b));

        bookingUserService.confirmBookingPayment(id, "pi_1", "{}");

        verify(bookingMapper, times(1)).toUserResponse(b);
        verify(paymentRepository, never()).findByProviderPaymentId(anyString());
        verify(paymentRepository, never()).save(any());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    public void initiateSinglePaymentExecution_Success_SavesPaymentAndReturnResponse()
    {
        UUID id=UUID.randomUUID();
        Booking b = new Booking();
        b.setId(id);
        b.setStatus(BookingStatus.PENDING);
        b.setTotalAmount(new BigDecimal("50.00"));
        when(bookingRepository.findById(id)).thenReturn(Optional.of(b));

        PaymentIntent pi=new PaymentIntent();
        pi.setId("pi_test_123");

        when(paymentGatewayService.createPaymentIntent(any(), any())).thenReturn(pi);
        bookingUserService.initiateSinglePaymentExecution(id,"idem_1");

        verify(paymentRepository,times(1)).save(any());
        verify(bookingMapper,times(1)).toUserResponse(eq(b),eq(pi));
    }

    @Test
    public void verifyAndHoldInventory_WhenLockAcquisitionFails_ShouldThrow()
    {
        UUID seatId=UUID.randomUUID();
        User user=new User();
        user.setId(UUID.randomUUID());

        when(redisLockService.acquireSeatLocks(anyList(),any(),anyInt())).thenReturn(false);

        try{
            bookingUserService.verifyAndHoldInventory(List.of(seatId),user, OffsetDateTime.now().plusMinutes(10));
            throw new AssertionError("excepted exception not thrown");
        }
        catch(Exception e)
        {
            // expected
        }

        verify(seatRepository,never()).findAndLockSeatsByIds(anyList());
    }
}
