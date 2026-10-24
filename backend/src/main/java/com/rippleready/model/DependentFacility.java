package com.rippleready.model;

public record DependentFacility(
        String id,
        String name,
        FacilityType type,
        double latitude,
        double longitude,
        String address,
        String contactPerson,
        String contactPhone,
        int beneficiaryCount,
        int bufferHours,
        String primaryNeed,
        double estimatedDailyLossZar,
        boolean verified,
        String status
) {
    public DependentFacility(
            String id,
            String name,
            FacilityType type,
            double latitude,
            double longitude,
            String address,
            String contactPerson,
            String contactPhone,
            int beneficiaryCount,
            int bufferHours,
            String primaryNeed,
            double estimatedDailyLossZar,
            boolean verified
    ) {
        this(id, name, type, latitude, longitude, address, contactPerson, contactPhone,
             beneficiaryCount, bufferHours, primaryNeed, estimatedDailyLossZar, verified, "STABLE");
    }

    public DependentFacility withStatus(String newStatus) {
        return new DependentFacility(
                id,
                name,
                type,
                latitude,
                longitude,
                address,
                contactPerson,
                contactPhone,
                beneficiaryCount,
                bufferHours,
                primaryNeed,
                estimatedDailyLossZar,
                verified,
                newStatus
        );
    }
}
