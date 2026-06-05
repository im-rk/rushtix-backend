package com.rushtix.core.feature.booking.service;

import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentGatewayService {
    private final StripeClient stripeClient;

    public PaymentIntent createPaymentIntent(UUID bookingId, BigDecimal totalAmount) {
        long amountInSmallestUnit= totalAmount.multiply(BigDecimal.valueOf(100)).longValue();

        PaymentIntentCreateParams params=PaymentIntentCreateParams.builder()
                .setAmount(amountInSmallestUnit)
                .setCurrency("inr")
                .putMetadata("booking_id", bookingId.toString())
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .build()
                )
                .build();
        try {
            log.info("Initiating Stripe transaction pipeline for Booking ID: {} for amount: {}", bookingId, amountInSmallestUnit);
            return stripeClient.paymentIntents().create(params);
        } catch (StripeException e) {
            log.error("Stripe gateway handshaking execution failure: ", e);
            throw new RuntimeException("Payment processing network gateway failure. Please try again.");
        }
    }
}
