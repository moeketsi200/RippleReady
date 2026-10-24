package com.rippleready.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record PledgeRequest(
        @NotBlank String resourceId,
        @NotBlank String facilityId,
        @Positive double allocatedUnits,
        String pledgerContact,
        String driverMobile,
        String verificationToken,
        String notes
) {
    public PledgeRequest(String resourceId, String facilityId, double allocatedUnits, String pledgerContact, String notes) {
        this(resourceId, facilityId, allocatedUnits, pledgerContact, "+27 82 555 9901", null, notes);
    }
}
