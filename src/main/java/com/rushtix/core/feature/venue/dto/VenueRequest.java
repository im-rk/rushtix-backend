package com.rushtix.core.feature.venue.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;


public record VenueRequest(

        @NotBlank(message = "Venue name cannot be blank")
        @Size(min = 2, max = 500, message = "Name must be between 2 and 500 characters")
        String name,

        @NotBlank(message = "City cannot be blank")
        @Size(max = 100)
        String city,

        @NotBlank(message = "State cannot be blank")
        @Size(max = 100)
        String state,

        @NotBlank(message = "Address line cannot be blank")
        String addressLine,

        @Size(max = 10)
        String pincode, // Optional, but if provided, limit size

        @Min(value = 10, message = "Total capacity must be at least 10")
        int totalCapacity,

        @NotBlank(message = "Timezone cannot be blank (e.g., Asia/Kolkata)")
        @Size(max = 50)
        String timezone,

        // GPS coordinates (Optional during initial creation)
        BigDecimal latitude,
        BigDecimal longitude

) {}