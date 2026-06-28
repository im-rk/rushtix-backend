package com.rushtix.core.feature.grouppay.api;

import com.rushtix.core.feature.grouppay.service.GroupPaySagaOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/grouppay/group-payment-webhook")
@RequiredArgsConstructor
@Slf4j
public class GroupPaymentWebhookController {
    private final GroupPaySagaOrchestrator orchestrator;

    @PostMapping("/webhook")
    public ResponseEntity<String> handleStripeCheckoutWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        try {

            UUID parsedPaymentItemId = UUID.fromString("COORDINATED_FROM_STRIPE_METADATA");
            String intentId = "ch_mock_stripe_intent_123";

            orchestrator.processWebhookPaymentSuccess(parsedPaymentItemId, intentId);
            return ResponseEntity.ok("Event received and processed by Saga lifecycle state machine");
        } catch (Exception e) {
            log.error("Webhook processing dropped structural parsing exceptions", e);
            return ResponseEntity.status(400).body("Webhook Exception handling log parsing error");
        }
    }
}
