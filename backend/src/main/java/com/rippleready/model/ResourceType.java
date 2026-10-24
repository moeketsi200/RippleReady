package com.rippleready.model;

public enum ResourceType {
    BOREHOLE_NON_POTABLE("Private/Commercial Borehole (Non-potable)"),
    JOJO_POTABLE("Treated JoJo Tank (Potable)"),
    RAIN_HARVEST("Rainwater Harvest System"),
    MOBILE_BOWSER("Mobile Water Bowser / Tanker"),
    BACKUP_GENERATOR("Emergency Generator / Power Station"),
    WATER_PURIFICATION_KITS("Water Purification Tablets & Filter Kits");

    private final String displayName;

    ResourceType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

