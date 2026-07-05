package com.rushtix.core.feature.booking.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public interface BookingOrganizerProjection {
    String getBookingId();
    String getCustomerName();
    String getCustomerEmail();
    int getTicketCount();
    BigDecimal getTotalPrice();
    String getStatus();
    OffsetDateTime getCreatedAt();
}
