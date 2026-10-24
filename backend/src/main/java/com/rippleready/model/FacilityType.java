package com.rippleready.model;

public enum FacilityType {
    CRECHE("Early Childhood Development / Crèche", 1, "Life & Care"),
    ELDERLY_CARE("Frail Care / Elderly Home", 1, "Life & Care"),
    CLINIC("Primary Healthcare / Dialysis Clinic", 1, "Life & Care"),
    SOUP_KITCHEN("Community Kitchen / Food Relief", 2, "Food Security & Community"),
    FOOD_MERCHANT("SME Food Merchant / Bakery / Spaza (FNB Merchant)", 3, "Commercial & Merchant Continuity"),
    LOCAL_SME("Local Small Business / Laundry (FNB Speedpoint)", 3, "Commercial & Merchant Continuity"),
    RESIDENTIAL_CLUSTER("Vulnerable Residential Cluster", 4, "Residential");

    private final String displayName;
    private final int priorityTier;
    private final String tierDescription;

    FacilityType(String displayName, int priorityTier, String tierDescription) {
        this.displayName = displayName;
        this.priorityTier = priorityTier;
        this.tierDescription = tierDescription;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getPriorityTier() {
        return priorityTier;
    }

    public String getTierDescription() {
        return tierDescription;
    }
}
