package com.rippleready.model;

import java.time.Instant;

public record OutageReport(
        String id,
        String reporterName,
        String reporterPhone,
        OutageCategory category,
        OutageSeverity severity,
        double latitude,
        double longitude,
        String wardNumber,
        String locationDescription,
        String notes,
        Instant reportedAt,
        boolean verified
) {}

