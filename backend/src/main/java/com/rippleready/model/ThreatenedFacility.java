package com.rippleready.model;

public record ThreatenedFacility(
        DependentFacility facility,
        double distanceKm,
        UrgencyLevel urgencyLevel,
        int estimatedRunwayMinutes,
        double projectedEconomicLossZar,
        String cascadeRiskSummary
) {}

