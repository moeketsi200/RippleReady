package com.rippleready.service;

import com.rippleready.model.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class RippleEngineService {

    private static final double EARTH_RADIUS_KM = 6371.0;
    private final DataStore dataStore;

    public RippleEngineService(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    /**
     * Calculates great-circle distance between two geographical points using Haversine formula.
     */
    public double calculateDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return Math.round(EARTH_RADIUS_KM * c * 100.0) / 100.0;
    }

    /**
     * Epic 2: Evaluates downstream cascade interdependency within outage perimeter.
     * US-2.1: Identifies critical facilities within ripple zone and assigns CRITICAL if buffer <= 2 hrs.
     * US-2.2: Ranks facilities by:
     *   1. Life & Care (Daycares, Frail Care, Clinics)
     *   2. Food Security & Community (Soup Kitchens, Feeding Schemes)
     *   3. Commercial & Merchant Continuity (Township Bakeries, Spaza Shops, FNB Speedpoint Merchants).
     */
    public RippleSimulationResponse evaluateImpact(RippleSimulationRequest request) {
        long startTime = System.currentTimeMillis();

        double impactRadius = request.effectiveImpactRadius();
        double sourcingRadius = request.effectiveSourcingRadius();
        double durationHours = request.effectiveDurationHours();

        List<ThreatenedFacility> threatened = new ArrayList<>();
        int criticalCount = 0;
        int highCount = 0;
        int moderateCount = 0;
        int totalBeneficiaries = 0;
        double totalLossZar = 0.0;

        // 1. Identify all dependent facilities inside outage ripple zone
        for (DependentFacility facility : dataStore.getAllFacilities()) {
            double distance = calculateDistanceKm(
                    request.latitude(), request.longitude(),
                    facility.latitude(), facility.longitude()
            );

            // Exclude facilities outside immediate perimeter (e.g. 5.5 km hospital when radius is 2.0 km)
            if (distance <= impactRadius) {
                UrgencyLevel urgency = determineUrgency(facility.bufferHours(), durationHours);

                if (urgency == UrgencyLevel.CRITICAL) {
                    criticalCount++;
                } else if (urgency == UrgencyLevel.HIGH) {
                    highCount++;
                } else if (urgency == UrgencyLevel.MODERATE) {
                    moderateCount++;
                }

                int runwayMinutes = facility.bufferHours() * 60;
                double projectedLoss = calculateProjectedLoss(facility, durationHours);
                String riskSummary = generateRiskSummary(facility, runwayMinutes);

                threatened.add(new ThreatenedFacility(
                        facility,
                        distance,
                        urgency,
                        runwayMinutes,
                        projectedLoss,
                        riskSummary
                ));

                totalBeneficiaries += facility.beneficiaryCount();
                totalLossZar += projectedLoss;
            }
        }

        // US-2.2: Prioritize facilities strictly by:
        // 1. Priority Tier: Life & Care (1) -> Food Security (2) -> Commercial / FNB Merchant (3)
        // 2. Urgency level
        // 3. Proximity distance
        threatened.sort(Comparator
                .comparingInt((ThreatenedFacility tf) -> tf.facility().type().getPriorityTier())
                .thenComparing((ThreatenedFacility tf) -> tf.urgencyLevel().ordinal())
                .thenComparingDouble(ThreatenedFacility::distanceKm));

        // 2. Discover and match available mutual aid within sourcing radius
        List<MatchedResource> matchedAid = new ArrayList<>();
        List<MutualAidResource> allResources = dataStore.getAllResources();

        for (ThreatenedFacility tf : threatened) {
            MutualAidResource bestMatch = findClosestSuitableResource(
                    tf.facility().latitude(),
                    tf.facility().longitude(),
                    sourcingRadius,
                    allResources,
                    tf.facility().type()
            );

            if (bestMatch != null) {
                double resDistance = calculateDistanceKm(
                        tf.facility().latitude(), tf.facility().longitude(),
                        bestMatch.latitude(), bestMatch.longitude()
                );

                int arrivalMinutes = (int) Math.round(8 + (resDistance * 2.5));
                double allocationUnits = calculateSuggestedAllocation(tf.facility());

                matchedAid.add(new MatchedResource(
                        bestMatch,
                        resDistance,
                        arrivalMinutes,
                        allocationUnits,
                        tf.facility().id(),
                        tf.facility().name(),
                        "READY_FOR_DISPATCH"
                ));
            }
        }

        long executionTimeMs = System.currentTimeMillis() - startTime;

        return new RippleSimulationResponse(
                request,
                threatened.size(),
                criticalCount,
                highCount,
                moderateCount,
                totalBeneficiaries,
                Math.round(totalLossZar * 100.0) / 100.0,
                threatened,
                matchedAid,
                executionTimeMs,
                Instant.now()
        );
    }

    /**
     * Epic 3 / US-3.1: Automated matching of borehole and JoJo capacity within search radius.
     * Returns matching providers ranked ascending by distance and validates volume capacity.
     */
    public List<MatchedResource> findMatchingResources(
            double targetLat,
            double targetLon,
            double radiusKm,
            double requiredVolume
    ) {
        List<MatchedResource> matches = new ArrayList<>();

        for (MutualAidResource res : dataStore.getAllResources()) {
            double dist = calculateDistanceKm(targetLat, targetLon, res.latitude(), res.longitude());

            if (dist <= radiusKm && res.availableCapacity() >= requiredVolume) {
                int arrivalMinutes = (int) Math.round(8 + (dist * 2.5));
                matches.add(new MatchedResource(
                        res,
                        dist,
                        arrivalMinutes,
                        requiredVolume,
                        null,
                        null,
                        "AVAILABLE"
                ));
            }
        }

        // Rank ascending by distance
        matches.sort(Comparator.comparingDouble(MatchedResource::distanceKm));
        return matches;
    }

    private UrgencyLevel determineUrgency(int bufferHours, double outageDurationHours) {
        // US-2.1: buffer <= 2 hrs is marked CRITICAL
        if (bufferHours <= 2.0 || outageDurationHours >= (bufferHours * 3)) {
            return UrgencyLevel.CRITICAL;
        } else if (bufferHours <= 3.5) {
            return UrgencyLevel.HIGH;
        } else if (bufferHours <= 6.0) {
            return UrgencyLevel.MODERATE;
        }
        return UrgencyLevel.LOW;
    }

    private double calculateProjectedLoss(DependentFacility facility, double outageDurationHours) {
        if (outageDurationHours <= facility.bufferHours()) {
            return 0.0;
        }
        double unbufferedHours = outageDurationHours - facility.bufferHours();
        double loss = facility.estimatedDailyLossZar() * Math.min(1.0, unbufferedHours / 8.0);
        return Math.round(loss * 100.0) / 100.0;
    }

    private String generateRiskSummary(DependentFacility facility, int runwayMinutes) {
        return switch (facility.type()) {
            case CRECHE -> String.format(
                    "Buffer expires in %d mins: %d toddlers without potable drinking water & basic sanitation.",
                    runwayMinutes, facility.beneficiaryCount()
            );
            case ELDERLY_CARE -> String.format(
                    "CRITICAL MEDICAL RUNWAY (%d mins): %d frail residents vulnerable to sanitation & hydration lapse.",
                    runwayMinutes, facility.beneficiaryCount()
            );
            case FOOD_MERCHANT -> String.format(
                    "FNB Merchant Buffer %d mins: %d lunch patrons affected; prep halt causes food spoilage & revenue loss.",
                    runwayMinutes, facility.beneficiaryCount()
            );
            case SOUP_KITCHEN -> String.format(
                    "Lunch service at risk (%d mins): %d food-insecure community members face meal cancellation.",
                    runwayMinutes, facility.beneficiaryCount()
            );
            default -> String.format(
                    "Operational buffer %d mins: %s facing trading disruption.",
                    runwayMinutes, facility.name()
            );
        };
    }

    private MutualAidResource findClosestSuitableResource(
            double targetLat, double targetLon,
            double sourcingRadius,
            List<MutualAidResource> resources,
            FacilityType facilityType
    ) {
        MutualAidResource closest = null;
        double minDistance = Double.MAX_VALUE;

        for (MutualAidResource res : resources) {
            if (res.availableCapacity() <= 0) {
                continue;
            }

            double dist = calculateDistanceKm(targetLat, targetLon, res.latitude(), res.longitude());
            if (dist <= sourcingRadius && dist < minDistance) {
                minDistance = dist;
                closest = res;
            }
        }
        return closest;
    }

    private double calculateSuggestedAllocation(DependentFacility facility) {
        return switch (facility.type()) {
            case CRECHE -> 150.0;
            case ELDERLY_CARE -> 300.0;
            case SOUP_KITCHEN -> 250.0;
            case FOOD_MERCHANT -> 200.0;
            default -> 100.0;
        };
    }
}
