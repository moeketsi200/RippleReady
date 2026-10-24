package com.rippleready.model;

import java.time.Instant;

public record IncidentRecord(
        String id,
        String reporterId,
        String category,
        String severity,
        double latitude,
        double longitude,
        String ward,
        String status,
        double impactRadiusKm,
        int confidenceScore,
        int confirmationVotes,
        Instant reportedAt,
        boolean isDuplicate,
        String linkedIncidentId
) {
    public IncidentRecord withConfirmationVote() {
        return new IncidentRecord(
                id,
                reporterId,
                category,
                severity,
                latitude,
                longitude,
                ward,
                status,
                impactRadiusKm,
                confidenceScore + 25,
                confirmationVotes + 1,
                reportedAt,
                false,
                null
        );
    }
}

