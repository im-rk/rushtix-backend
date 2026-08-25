package com.rushtix.core.unit;

import com.rushtix.core.domain.entities.Event;
import com.rushtix.core.feature.booking.api.StripeWebhookController;
import com.rushtix.core.feature.booking.service.BookingUserService;
import com.rushtix.core.feature.grouppay.service.GroupPaySagaOrchestrator;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StripeWebhookControllerTest {

    @Mock
    BookingUserService userService;

    @Mock
    GroupPaySagaOrchestrator groupPaySagaOrchestrator;

    @InjectMocks
    StripeWebhookController stripeWebhookController;

    @Test
    public void handleStripeWebhook_InvalidSignature_ShouldThrowBadRequest() throws Exception {
        try (MockedStatic<Webhook> stub = mockStatic(Webhook.class)) {
            stub.when(() -> Webhook.constructEvent(anyString(), anyString(), anyString()))
                    .thenThrow(new SignatureVerificationException("sig invalid", null));
            try {
                stripeWebhookController.handleStripeWebhook("payload", "sig");
                throw new AssertionError("expected");
            } catch (ResponseStatusException ex) {
                // expected: invalid signature -> 400
            }
        }
    }

//    @Test
//    public void handleStripeWebhook_PaymentIntentSucceeded_ShouldCallBookingConfirm() throws Exception {
//        Event mockEvent = mock(Event.class);
//        EventDataObjectDeserializer mockD = mock(EventDataObjectDeserializer.class);
//        PaymentIntent pi = new PaymentIntent();
//        pi.setId("pi_1");
//        pi.putMetadata("booking_id", "00000000-0000-0000-0000-000000000001");
//
//        when(mockEvent.getType()).thenReturn("payment_intent.succeeded");
//        when(mockEvent.getDataObjectDeserializer()).thenReturn(mockD);
//        when(mockD.deserializeUnsafe()).thenReturn(pi);
//
//        try (MockedStatic<Webhook> stub = mockStatic(Webhook.class)) {
//            stub.when(() -> Webhook.constructEvent(anyString(), anyString(), anyString())).thenReturn(mockEvent);
//            controller.handleStripeWebhook("payload", "sig");
//            // bookingUserService.confirmBookingPayment called only when booking_id present;
//            verify(bookingUserService, atMost(1)).confirmBookingPayment(any(), anyString(), anyString());
//        }
//    }



}
