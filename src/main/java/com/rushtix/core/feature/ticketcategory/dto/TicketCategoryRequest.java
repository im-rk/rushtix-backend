package com.rushtix.core.feature.ticketcategory.dto;

import com.rushtix.core.domain.enums.PricingAlgorithm;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record TicketCategoryRequest(
        @NotBlank(message = "Category name cannot be blank")
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        String name,

        @NotNull(message = "Base price cannot be null")
        @DecimalMin(value = "0.01", message = "Base price must be greater than 0")
        @Digits(integer = 10, fraction = 2, message = "Price must have at most 10 integer digits and 2 decimal places")
        BigDecimal basePrice,

        @NotNull(message = "Min price cannot be null")
        @DecimalMin(value = "0.01", message = "Min price must be greater than 0")
        @Digits(integer = 10, fraction = 2, message = "Price must have at most 10 integer digits and 2 decimal places")
        BigDecimal minPrice,

        @NotNull(message = "Max price cannot be null")
        @DecimalMin(value = "0.01", message = "Max price must be greater than 0")
        @Digits(integer = 10, fraction = 2, message = "Price must have at most 10 integer digits and 2 decimal places")
        BigDecimal maxPrice,

        @NotNull(message = "Capacity cannot be null")
        @Min(value = 1, message = "Capacity must be at least 1")
        @Max(value = 100000, message = "Capacity cannot exceed 100,000")
        int capacity,

        @NotNull(message = "Display order cannot be null")
        @Min(value = 1, message = "Display order must be at least 1")
        int displayOrder,

        @NotNull(message = "Pricing algorithm cannot be null")
        PricingAlgorithm pricingAlgorithm,

        boolean dynamicPricingEnabled
) {}
