package com.rippleready.model;

public record ImpactSummaryResponse(
        int totalIncidentsIntercepted,
        int secondaryCrisesAverted,
        int totalBeneficiariesProtected,
        double totalEconomicLossAvertedZar,
        double totalWaterAllocatedLitres,
        int activeMutualAidPledges,
        double averageResponseTimeMinutes,
        String pilotWard
) {}

