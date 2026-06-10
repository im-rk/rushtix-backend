package com.rushtix.core.feature.ticketcategory.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record PublicTicketCategoryResponse(
        UUID categoryId,
        String name,
        String description,
        BigDecimal currentPrice,
        boolean isSoldOut
) {}
