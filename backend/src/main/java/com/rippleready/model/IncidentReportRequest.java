package com.rippleready.model;

public record IncidentReportRequest(
        String reporterId,
        String category,
        Double latitude,
        Double longitude,
        String severity,
        String ward,
        String locationDescription
) {}

