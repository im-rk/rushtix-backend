package com.rushtix.core.feature.booking.service;

import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
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

    public String createStripeCheckoutSessionUrl(UUID groupPaymentItemId, BigDecimal itemAmount) {
        long amountInSmallestUnit = itemAmount.multiply(BigDecimal.valueOf(100)).longValue();

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("https://rushtix.com/booking/group/success?itemId=" + groupPaymentItemId)
                .setCancelUrl("https://rushtix.com/booking/group/cancel")
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("inr")
                                                .setUnitAmount(amountInSmallestUnit)
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("RushTix Group Split Ticket Pass")
                                                                .setDescription("Your split share for the group booking event.")
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .putMetadata("group_payment_item_id", groupPaymentItemId.toString())
                .build();

        try {
            log.info("Generating Hosted Stripe Checkout Session for Split Item ID: {}", groupPaymentItemId);
            // Reusing your injected stripeClient beans perfectly
            Session session = stripeClient.checkout().sessions().create(params);
            return session.getUrl();
        } catch (StripeException e) {
            log.error("Failed to generate Stripe Checkout Session link: ", e);
            throw new RuntimeException("Payment gateway link initialization failed. Please try again.");
        }
    }
}
