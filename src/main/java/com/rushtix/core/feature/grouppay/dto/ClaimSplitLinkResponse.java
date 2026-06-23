package com.rushtix.core.feature.grouppay.dto;

public record ClaimSplitLinkResponse(
        String stripeCheckoutUrl,
        String message
) {
}
