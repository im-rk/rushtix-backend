package com.rushtix.core.unit;

import com.rushtix.core.feature.booking.service.PaymentGatewayService;
import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PaymentGatewayServiceTest {
    @Mock
    private StripeClient stripeClient;

    @InjectMocks private PaymentGatewayService svc;

    @Test
    public void createPaymentIntent_WhenStripeThrows_ShouldWrapAndThrowRuntimeException() throws Exception {
        UUID bookingId = UUID.randomUUID();
        when(stripeClient.paymentIntents()).thenThrow(new StripeException("down", null, null, 500, null));
        assertThrows(RuntimeException.class, () -> svc.createPaymentIntent(bookingId, new BigDecimal("10.00")));
    }
}
