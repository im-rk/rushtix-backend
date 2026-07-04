package com.rushtix.core.feature.booking.api;

import com.rushtix.core.domain.entities.Event;
import com.rushtix.core.feature.booking.service.BookingUserService;
import com.rushtix.core.feature.grouppay.service.GroupPaySagaOrchestrator;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
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
    private final GroupPaySagaOrchestrator groupPaySagaOrchestrator;
    
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
        log.info("Received Stripe webhook event: {}", event.getType());

        if ("payment_intent.succeeded".equals(event.getType())) {
            handlePaymentIntentSucceeded(event, payload);
        } else if ("checkout.session.completed".equals(event.getType())) {
            handleCheckoutSessionCompleted(event, payload);
        }
    }

    private void handlePaymentIntentSucceeded(com.stripe.model.Event event, String payload) {
        try {
            com.stripe.model.StripeObject stripeObject = event.getDataObjectDeserializer().deserializeUnsafe();
            if (stripeObject instanceof PaymentIntent paymentIntent) {
                String bookingIdStr = paymentIntent.getMetadata().get("booking_id");
                if (bookingIdStr != null) {
                    UUID bookingId = UUID.fromString(bookingIdStr);
                    log.info("Webhook alert: Verified payment received for Booking ID: {}. Processing fulfillment...", bookingId);
                    bookingUserService.confirmBookingPayment(bookingId, paymentIntent.getId(), payload);
                } else {
                    log.warn("PaymentIntent {} succeeded but missing booking_id in metadata (might be a Group Pay payment)", paymentIntent.getId());
                }
            } else {
                log.error("Failed to deserialize PaymentIntent from webhook event payload");
            }
        } catch (com.stripe.exception.EventDataObjectDeserializationException e) {
            log.warn("Stripe webhook deserialization failed due to API version mismatch: {}. Falling back to raw JSON.", e.getMessage());
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(payload);
                com.fasterxml.jackson.databind.JsonNode dataObject = root.path("data").path("object");
                
                com.fasterxml.jackson.databind.JsonNode metadata = dataObject.path("metadata");
                if (!metadata.isMissingNode() && metadata.has("booking_id")) {
                    String bookingIdStr = metadata.get("booking_id").asText();
                    String paymentIntentId = dataObject.has("id") ? dataObject.get("id").asText() : "unknown_pi";
                    UUID bookingId = UUID.fromString(bookingIdStr);
                    log.info("Webhook alert (Raw JSON Fallback): Verified payment received for Booking ID: {}. Processing fulfillment...", bookingId);
                    bookingUserService.confirmBookingPayment(bookingId, paymentIntentId, payload);
                } else {
                    log.warn("Raw JSON Fallback: metadata or booking_id is missing (might be a Group Pay payment).");
                }
            } catch (Exception ex) {
                log.error("Failed to parse raw JSON payload with Jackson: {}", ex.getMessage());
            }
        }
    }

    private void handleCheckoutSessionCompleted(com.stripe.model.Event event, String payload) {
        try {
            com.stripe.model.StripeObject stripeObject = event.getDataObjectDeserializer().deserializeUnsafe();
            if (stripeObject instanceof Session session) {
                String paymentItemIdStr = session.getMetadata().get("group_payment_item_id");
                if (paymentItemIdStr != null) {
                    UUID paymentItemId = UUID.fromString(paymentItemIdStr);
                    log.info("Webhook alert: Verified payment received for Group Payment Item ID: {}. Processing fulfillment...", paymentItemId);
                    groupPaySagaOrchestrator.processWebhookPaymentSuccess(paymentItemId, session.getPaymentIntent());
                } else {
                    log.warn("Session {} succeeded but missing group_payment_item_id in metadata", session.getId());
                }
            } else {
                log.error("Failed to deserialize Session from webhook event payload");
            }
        } catch (com.stripe.exception.EventDataObjectDeserializationException e) {
            log.warn("Stripe webhook deserialization failed due to API version mismatch: {}. Falling back to raw JSON.", e.getMessage());
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(payload);
                com.fasterxml.jackson.databind.JsonNode dataObject = root.path("data").path("object");
                
                com.fasterxml.jackson.databind.JsonNode metadata = dataObject.path("metadata");
                if (!metadata.isMissingNode() && metadata.has("group_payment_item_id")) {
                    String paymentItemIdStr = metadata.get("group_payment_item_id").asText();
                    String paymentIntentId = dataObject.has("payment_intent") ? dataObject.get("payment_intent").asText() : "unknown_pi";
                    UUID paymentItemId = UUID.fromString(paymentItemIdStr);
                    log.info("Webhook alert (Raw JSON Fallback): Verified payment received for Group Payment Item ID: {}. Processing fulfillment...", paymentItemId);
                    groupPaySagaOrchestrator.processWebhookPaymentSuccess(paymentItemId, paymentIntentId);
                } else {
                    log.error("Raw JSON Fallback failed: metadata or group_payment_item_id is missing.");
                }
            } catch (Exception ex) {
                log.error("Failed to parse raw JSON payload with Jackson: {}", ex.getMessage());
            }
        }
    }
}

