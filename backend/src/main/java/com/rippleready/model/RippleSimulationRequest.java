package com.rippleready.model;

import jakarta.validation.constraints.NotNull;

public record RippleSimulationRequest(
        @NotNull OutageCategory category,
        @NotNull OutageSeverity severity,
        double latitude,
        double longitude,
        String wardNumber,
        String locationDescription,
        Double estimatedOutageDurationHours,
        Double impactRadiusKm,
        Double sourcingRadiusKm
) {
    public double effectiveImpactRadius() {
        return (impactRadiusKm != null && impactRadiusKm > 0) ? impactRadiusKm : 2.5;
    }

    public double effectiveSourcingRadius() {
        return (sourcingRadiusKm != null && sourcingRadiusKm > 0) ? sourcingRadiusKm : 4.0;
    }

    public double effectiveDurationHours() {
        return (estimatedOutageDurationHours != null && estimatedOutageDurationHours > 0)
                ? estimatedOutageDurationHours : 6.0;
    }
}

