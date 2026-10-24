package com.rippleready.model;

import jakarta.validation.constraints.NotBlank;

public record DeliveryVerificationRequest(
        @NotBlank String verificationToken,
        String recipientSignoff,
        String recipientNotes,
        Double roundTripKm
) {
    public double effectiveRoundTripKm() {
        return (roundTripKm != null && roundTripKm > 0) ? roundTripKm : 4.0;
    }
}

