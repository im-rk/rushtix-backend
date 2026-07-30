package com.rushtix.core.unit;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private BookingMapper bookingMapper;

    // We inject the mocks into YOUR actual service name
    @InjectMocks
    private BookingUserService bookingUserService;

    @Test
    public void confirmBookingPayment_WhenBookingNotPending_ShouldReturnEarlyWithoutSaving() {

        UUID fakeBookingId = UUID.randomUUID();
        String fakeProviderId = "pi_123xyz";
        String fakeMetadata = "{}";

        Booking fakeBooking = new Booking();
        fakeBooking.setId(fakeBookingId);
        fakeBooking.setStatus(BookingStatus.CONFIRMED);

        when(bookingRepository.findById(fakeBookingId)).thenReturn(Optional.of(fakeBooking));

        bookingUserService.confirmBookingPayment(fakeBookingId, fakeProviderId, fakeMetadata);

        verify(bookingMapper, times(1)).toUserResponse(fakeBooking);

        verify(paymentRepository, never()).findByProviderPaymentId(anyString());

        verify(paymentRepository, never()).save(any());
        verify(bookingRepository, never()).save(any());
    }
}
