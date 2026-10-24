package com.rippleready.model;

public record MatchedResource(
        MutualAidResource resource,
        double distanceKm,
        int estimatedArrivalMinutes,
        double suggestedAllocationUnits,
        String matchedFacilityId,
        String matchedFacilityName,
        String status
) {}

