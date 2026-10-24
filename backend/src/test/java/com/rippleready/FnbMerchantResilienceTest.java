package com.rippleready;

import com.rippleready.model.*;
import com.rippleready.service.DataStore;
import com.rippleready.service.RippleEngineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class FnbMerchantResilienceTest {

    @Autowired
    private DataStore dataStore;

    @Autowired
    private RippleEngineService rippleEngine;

    @BeforeEach
    void setUp() {
        dataStore.reset();
    }

    @Test
    @DisplayName("FNB Commercial Value: Preserves Speedpoint merchant daily cash flow during a 6-hour water outage")
    void testFnbSpeedpointTurnoverProtection() {
        RippleSimulationRequest request = new RippleSimulationRequest(
                OutageCategory.WATER,
                OutageSeverity.CRITICAL_MAIN_RUPTURE,
                -33.9620,
                22.4620,
                "Ward 18, George",
                "York Street primary feeder rupture",
                6.0,
                2.5,
                4.0
        );

        RippleSimulationResponse response = rippleEngine.evaluateImpact(request);

        // Identify local FNB Speedpoint merchants (Mama K's Spaza and York Street Laundromat)
        List<ThreatenedFacility> commercialNodes = response.threatenedFacilities().stream()
                .filter(tf -> tf.facility().type() == FacilityType.FOOD_MERCHANT || tf.facility().type() == FacilityType.LOCAL_SME)
                .toList();

        assertThat(commercialNodes).hasSizeGreaterThanOrEqualTo(2);

        double totalCommercialLossRisk = commercialNodes.stream()
                .mapToDouble(ThreatenedFacility::projectedEconomicLossZar)
                .sum();

        // Preserves over R3,000 of immediate small-merchant turnover (exact: R3,350.0)
        assertThat(totalCommercialLossRisk).isEqualTo(3350.0);

        // Verify risk summary explicitly flags merchant continuity
        for (ThreatenedFacility merchant : commercialNodes) {
            assertThat(merchant.cascadeRiskSummary()).containsAnyOf("patrons affected", "trading disruption");
        }
    }

    @Test
    @DisplayName("FNB Commercial Value: Highlighting township spaza buffer runway before stock spoilage")
    void testMerchantSurvivalRunwayBuffer() {
        DependentFacility mamaK = dataStore.getFacilityById("fac-2")
                .orElseThrow(() -> new AssertionError("Mama K's Spaza fac-2 not found"));

        assertThat(mamaK.type()).isEqualTo(FacilityType.FOOD_MERCHANT);
        assertThat(mamaK.bufferHours()).isEqualTo(3);
        assertThat(mamaK.estimatedDailyLossZar()).isEqualTo(4800.0);
    }

    @Test
    @DisplayName("FNB Commercial Value: Commercial nodes are prioritized at Tier 3 after Life & Care and Food Security")
    void testTier3MerchantRanking() {
        assertThat(FacilityType.FOOD_MERCHANT.getPriorityTier()).isEqualTo(3);
        assertThat(FacilityType.LOCAL_SME.getPriorityTier()).isEqualTo(3);
        assertThat(FacilityType.FOOD_MERCHANT.getTierDescription()).isEqualTo("Commercial & Merchant Continuity");
    }

    @Test
    @DisplayName("FNB Commercial Value: Rapid mutual aid matching protects Speedpoint trading continuity")
    void testRapidMatchingProtectsMerchantContinuity() {
        // Query for Mama K's eatery (~200L sanitation water needed)
        List<MatchedResource> matches = rippleEngine.findMatchingResources(
                -33.9615, 22.4640, 2.5, 200.0
        );

        assertThat(matches).isNotEmpty();
        // Closest provider is less than 1.5 km away
        assertThat(matches.get(0).distanceKm()).isLessThan(1.5);
        // Estimated transit time is under 15 minutes
        assertThat(matches.get(0).estimatedArrivalMinutes()).isLessThan(15);
    }
}
