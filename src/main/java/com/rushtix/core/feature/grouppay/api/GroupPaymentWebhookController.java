package com.rushtix.core.feature.grouppay.api;

import com.rushtix.core.feature.grouppay.service.GroupPaySagaOrchestrator;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/grouppay/group-payment-webhook")
@RequiredArgsConstructor
@Slf4j
public class GroupPaymentWebhookController {
    private final GroupPaySagaOrchestrator orchestrator;

    @Value("${stripe.webhook.secret}")
    private String endpointSecret;

    @PostMapping("/webhook")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> handleStripeCheckoutWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        com.stripe.model.Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
        } catch (SignatureVerificationException e) {
            log.error("GroupPay Stripe webhook signature verification failed: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Stripe webhook signature");
        }
        log.info("Received GroupPay Stripe webhook event");

        if ("checkout.session.completed".equals(event.getType())) {
            try {
                com.stripe.model.StripeObject stripeObject = event.getDataObjectDeserializer().deserializeUnsafe();
                if (stripeObject instanceof Session session) {
                    String paymentItemIdStr = session.getMetadata().get("group_payment_item_id");
                    if (paymentItemIdStr != null) {
                        UUID paymentItemId = UUID.fromString(paymentItemIdStr);
                        log.info("Webhook alert: Verified payment received for Group Payment Item ID: {}. Processing fulfillment...", paymentItemId);
                        orchestrator.processWebhookPaymentSuccess(paymentItemId, session.getPaymentIntent());
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
                        orchestrator.processWebhookPaymentSuccess(paymentItemId, paymentIntentId);
                    } else {
                        log.error("Raw JSON Fallback failed: metadata or group_payment_item_id is missing.");
                    }
                } catch (Exception ex) {
                    log.error("Failed to parse raw JSON payload with Jackson: {}", ex.getMessage());
                }
            }
        }

        return ResponseEntity.ok("Event received and processed by Saga lifecycle state machine");
    }
}
