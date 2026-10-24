package com.rippleready.model;

import java.time.Instant;

public record FnbDisbursementResult(
        String transactionReference,
        String recipientMobile,
        double amountZar,
        String paymentRail,
        String purpose,
        String status,
        Instant disbursedAt
) {}

