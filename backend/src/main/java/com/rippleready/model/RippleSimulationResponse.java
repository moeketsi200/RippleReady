package com.rippleready.model;

import java.time.Instant;
import java.util.List;

public record RippleSimulationResponse(
        RippleSimulationRequest outageRequest,
        int totalSecondaryRisks,
        int criticalRiskCount,
        int highRiskCount,
        int moderateRiskCount,
        int totalBeneficiariesThreatened,
        double totalProjectedEconomicLossZar,
        List<ThreatenedFacility> threatenedFacilities,
        List<MatchedResource> matchedMutualAid,
        long executionTimeMs,
        Instant generatedAt
) {}

