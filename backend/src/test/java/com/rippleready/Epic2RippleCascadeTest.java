package com.rippleready;

import com.rippleready.model.*;
import com.rippleready.service.DataStore;
import com.rippleready.service.RippleEngineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class Epic2RippleCascadeTest {

    private DataStore dataStore;
    private RippleEngineService rippleEngine;

    @BeforeEach
    void setUp() {
        dataStore = new DataStore();
        rippleEngine = new RippleEngineService(dataStore);
    }

    @Test
    @DisplayName("US-2.1: Identify critical facilities within the 2.0 km ripple zone and exclude distant nodes")
    void testUS2_1_IdentifyCriticalFacilitiesWithin2kmRippleZone() {
        // Active water outage at (-33.9630, 22.4610) with impact radius 2.0 km
        RippleSimulationRequest request = new RippleSimulationRequest(
                OutageCategory.WATER,
                OutageSeverity.CRITICAL_MAIN_RUPTURE,
                -33.9630,
                22.4610,
                "Ward 18, George",
                "York Street feeder rupture",
                6.0,
                2.0, // Strict 2.0 km perimeter
                3.5
        );

        RippleSimulationResponse response = rippleEngine.evaluateImpact(request);

        assertThat(response.threatenedFacilities()).isNotEmpty();

        // 1. "Little Stars Crèche" at (-33.9632, 22.4612) is included with CRITICAL urgency (buffer <= 2h)
        ThreatenedFacility creche = response.threatenedFacilities().stream()
                .filter(tf -> tf.facility().name().contains("Little Stars Crèche"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Little Stars Crèche should be in the threatened facilities list"));

        assertThat(creche.urgencyLevel()).isEqualTo(UrgencyLevel.CRITICAL);
        assertThat(creche.distanceKm()).isLessThanOrEqualTo(2.0);

        // 2. "George Central Hospital" at (-33.9900, 22.5000, 5.5 km) is EXCLUDED from the 2.0 km cascade list
        boolean hasDistantHospital = response.threatenedFacilities().stream()
                .anyMatch(tf -> tf.facility().name().contains("George Central Hospital"));

        assertThat(hasDistantHospital).isFalse();
    }

    @Test
    @DisplayName("US-2.2: Prioritize business and livelihood continuity (FNB Merchant Node ranking)")
    void testUS2_2_PrioritizeBusinessAndLivelihoodContinuity() {
        RippleSimulationRequest request = new RippleSimulationRequest(
                OutageCategory.WATER,
                OutageSeverity.CRITICAL_MAIN_RUPTURE,
                -33.9620,
                22.4620,
                "Ward 18, George",
                "Wide area disruption",
                6.0,
                3.0, // captures all 5 local institutions
                4.0
        );

        RippleSimulationResponse response = rippleEngine.evaluateImpact(request);
        List<ThreatenedFacility> facilities = response.threatenedFacilities();

        assertThat(facilities.size()).isGreaterThanOrEqualTo(4);

        // Verify ordering conforms to:
        // Tier 1: Life & Care (Crèche, Elderly Care)
        // Tier 2: Food Security & Community (Soup Kitchen)
        // Tier 3: Commercial & Merchant Continuity (Mama K's Spaza, Laundromat / FNB Speedpoint Merchants)
        int lastTierSeen = 0;
        for (ThreatenedFacility tf : facilities) {
            int currentTier = tf.facility().type().getPriorityTier();
            assertThat(currentTier)
                    .as("Facility %s (Tier %d) must not precede a higher-priority tier", tf.facility().name(), currentTier)
                    .isGreaterThanOrEqualTo(lastTierSeen);
            lastTierSeen = currentTier;
        }

        // Specifically assert that Life & Care comes before Commercial Merchants
        int crecheIndex = findFacilityIndex(facilities, "Little Stars Crèche");
        int soupKitchenIndex = findFacilityIndex(facilities, "St. Mark's Community Soup Kitchen");
        int merchantIndex = findFacilityIndex(facilities, "Mama K's Spaza");

        assertThat(crecheIndex).isLessThan(soupKitchenIndex);
        assertThat(soupKitchenIndex).isLessThan(merchantIndex);
    }

    private int findFacilityIndex(List<ThreatenedFacility> list, String namePrefix) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).facility().name().contains(namePrefix)) {
                return i;
            }
        }
        return -1;
    }
}

