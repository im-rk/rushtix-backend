package com.rushtix.core.feature.booking.api;

import com.rushtix.core.domain.entities.Event;
import com.rushtix.core.feature.booking.service.BookingUserService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/webhooks/stripe")
@RequiredArgsConstructor
@Slf4j
public class StripeWebhookController {
    private final BookingUserService bookingUserService;
    @Value("${stripe.webhook.secret}")
    private String endpointSecret;

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public void handleStripeWebhook(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader) {
        com.stripe.model.Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
        } catch (SignatureVerificationException e) {
            log.error("Stripe webhook signature verification failed: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Stripe webhook signature");
        }
        log.info("Received Stripe webhook event");

        if ("payment_intent.succeeded".equals(event.getType())) {
            com.stripe.model.StripeObject stripeObject = event.getDataObjectDeserializer().deserializeUnsafe();
            if (stripeObject instanceof PaymentIntent paymentIntent) {
                String bookingIdStr = paymentIntent.getMetadata().get("booking_id");
                if (bookingIdStr != null) {
                    UUID bookingId = UUID.fromString(bookingIdStr);
                    log.info("Webhook alert: Verified payment received for Booking ID: {}. Processing fulfillment...", bookingId);
                    bookingUserService.confirmBookingPayment(bookingId, paymentIntent.getId(), payload);
                } else {
                    log.warn("PaymentIntent {} succeeded but missing booking_id in metadata", paymentIntent.getId());
                }
            } else {
                log.error("Failed to deserialize PaymentIntent from webhook event payload");
            }
        }
    }
}
