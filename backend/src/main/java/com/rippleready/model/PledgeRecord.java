package com.rippleready.model;

import java.time.Instant;

public record PledgeRecord(
        String id,
        String resourceId,
        String resourceProviderName,
        String facilityId,
        String facilityName,
        double allocatedUnits,
        String unitMeasure,
        String pledgerContact,
        String notes,
        String status,
        String verificationToken,
        Instant pledgedAt,
        Instant verifiedAt,
        String driverMobile,
        String fnbPaymentRef
) {
    public PledgeRecord(
            String id,
            String resourceId,
            String resourceProviderName,
            String facilityId,
            String facilityName,
            double allocatedUnits,
            String unitMeasure,
            String pledgerContact,
            String notes,
            String status,
            String verificationToken,
            Instant pledgedAt
    ) {
        this(id, resourceId, resourceProviderName, facilityId, facilityName,
             allocatedUnits, unitMeasure, pledgerContact, notes, status,
             verificationToken, pledgedAt, null, null, null);
    }

    public PledgeRecord withResolved(Instant verifiedAt, String fnbPaymentRef) {
        return new PledgeRecord(
                id, resourceId, resourceProviderName, facilityId, facilityName,
                allocatedUnits, unitMeasure, pledgerContact, notes, "RESOLVED",
                verificationToken, pledgedAt, verifiedAt, driverMobile, fnbPaymentRef
        );
    }
}
