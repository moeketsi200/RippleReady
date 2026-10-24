package com.rippleready.model;

public enum UrgencyLevel {
    CRITICAL("Immediate Risk (< 2h runway)"),
    HIGH("Elevated Risk (2h - 4h runway)"),
    MODERATE("Impending Risk (4h - 8h runway)"),
    LOW("Monitoring / Resilient (> 8h runway)");

    private final String description;

    UrgencyLevel(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

