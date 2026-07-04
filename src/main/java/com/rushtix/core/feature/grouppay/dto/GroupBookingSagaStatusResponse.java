package com.rushtix.core.feature.grouppay.dto;

import java.util.List;
import java.util.UUID;

public record GroupBookingSagaStatusResponse(
        UUID sagaId,
        String status,
        int totalSeats,
        int paidCount,
        List<PaymentItemStatus> paymentItems
) {
    public record PaymentItemStatus(
            UUID itemId,
            String friendEmail,
            String status,        // PENDING, PAID, REFUNDED
            String stripeCheckoutUrl  // Only returned if status is PENDING (not yet paid)
    ) {}
}
