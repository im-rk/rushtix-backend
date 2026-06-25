package com.rushtix.core.feature.grouppay.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record InitiateGroupResponse(
        UUID bookingId,
        String initiatorStripeUrl,
        OffsetDateTime expiresAt,
        String message
) {
}
