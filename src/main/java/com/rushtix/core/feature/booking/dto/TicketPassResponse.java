package com.rushtix.core.feature.booking.dto;

public record TicketPassResponse (
        String categoryName,
        String seatLabel,
        String qrtoken
){ }
