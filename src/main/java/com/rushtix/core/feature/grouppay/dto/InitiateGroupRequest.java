package com.rushtix.core.feature.grouppay.dto;

import java.util.List;
import java.util.UUID;

public record InitiateGroupRequest(
        UUID eventId,
        UUID initiatorId,
        List<UUID> seatIds
) {
}
