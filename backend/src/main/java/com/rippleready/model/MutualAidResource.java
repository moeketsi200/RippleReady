package com.rippleready.model;

public record MutualAidResource(
        String id,
        String providerName,
        ResourceType resourceType,
        double latitude,
        double longitude,
        String address,
        String contactPerson,
        String contactPhone,
        double totalCapacity,
        double availableCapacity,
        String unitMeasure,
        boolean verified,
        boolean requiresTransport
) {
    public MutualAidResource withAvailableCapacity(double newAvailableCapacity) {
        return new MutualAidResource(
                id,
                providerName,
                resourceType,
                latitude,
                longitude,
                address,
                contactPerson,
                contactPhone,
                totalCapacity,
                newAvailableCapacity,
                unitMeasure,
                verified,
                requiresTransport
        );
    }
}

