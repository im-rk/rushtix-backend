package com.rushtix.core.feature.grouppay.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ClaimSplitLinkRequest(
        @NotNull UUID groupBookingId,
        @Email String friendEmail
) {
}
