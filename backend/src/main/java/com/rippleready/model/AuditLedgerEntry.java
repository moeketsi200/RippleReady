package com.rippleready.model;

import java.time.Instant;

public record AuditLedgerEntry(
        String id,
        Instant timestamp,
        String eventType,
        String facilityId,
        String facilityName,
        String resourceId,
        String verificationToken,
        double litersDelivered,
        int beneficiariesProtected,
        String fnbTxnRef,
        String status,
        String auditHash
) {}

