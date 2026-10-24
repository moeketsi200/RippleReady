package com.rippleready.model;

import java.time.Instant;

public record DeliveryVerificationResponse(
        String pledgeId,
        String facilityId,
        String facilityName,
        String facilityStatus,
        String dashboardNodeColor,
        double litersDelivered,
        int beneficiariesProtected,
        FnbDisbursementResult fnbDisbursement,
        Instant verifiedAt
) {}

