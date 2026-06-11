package com.rushtix.core.feature.seat.dto;

import java.util.UUID;

public record PublicSeatResponse (
    UUID id,
    UUID categoryId,
    String rowLabel,
    String seatNumber,
    String displayLabel,
    boolean isAccessible,
    String status
){}

