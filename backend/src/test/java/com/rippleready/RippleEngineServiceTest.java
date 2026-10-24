package com.rippleready;

import com.rippleready.model.*;
import com.rippleready.service.DataStore;
import com.rippleready.service.RippleEngineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class RippleEngineServiceTest {

    private RippleEngineService rippleEngine;
    private DataStore dataStore;

    @BeforeEach
    void setUp() {
        dataStore = new DataStore();
        rippleEngine = new RippleEngineService(dataStore);
    }

    @Test
    @DisplayName("Should accurately calculate Haversine distance in kilometers")
    void testHaversineDistance() {
        // Distance between two known points in George, Western Cape (~630m)
        double lat1 = -33.9630;
        double lon1 = 22.4610;
        double lat2 = -33.9580;
        double lon2 = 22.4650;

        double distance = rippleEngine.calculateDistanceKm(lat1, lon1, lat2, lon2);
        assertThat(distance).isCloseTo(0.67, within(0.1));
    }

    @Test
    @DisplayName("Should identify threatened facilities and match nearby mutual aid during a pipe burst")
    void testEvaluateImpactWithCascade() {
        RippleSimulationRequest request = new RippleSimulationRequest(
                OutageCategory.WATER,
                OutageSeverity.CRITICAL_MAIN_RUPTURE,
                -33.9620, // York St epicenter
                22.4620,
                "Ward 18",
                "York Street Main Rupture",
                6.0,
                2.5,
                4.0
        );

        RippleSimulationResponse response = rippleEngine.evaluateImpact(request);

        assertThat(response).isNotNull();
        assertThat(response.totalSecondaryRisks()).isGreaterThan(0);
        assertThat(response.threatenedFacilities()).isNotEmpty();

        // Check Little Stars Crèche is flagged
        boolean hasCreche = response.threatenedFacilities().stream()
                .anyMatch(tf -> tf.facility().name().contains("Little Stars Crèche"));
        assertThat(hasCreche).isTrue();

        // Check mutual aid was discovered and matched
        assertThat(response.matchedMutualAid()).isNotEmpty();

        // Check execution performance (must be sub-second for hackathon)
        assertThat(response.executionTimeMs()).isLessThan(500);
    }
}

