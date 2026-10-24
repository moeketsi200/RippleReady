package com.rippleready;

import com.rippleready.model.*;
import com.rippleready.service.DataStore;
import com.rippleready.service.RippleEngineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class Epic2CascadeBoundaryAndPerformanceTest {

    private DataStore dataStore;
    private RippleEngineService rippleEngine;

    @BeforeEach
    void setUp() {
        dataStore = new DataStore();
        rippleEngine = new RippleEngineService(dataStore);
    }

    @Test
    @DisplayName("Cascade Duration: 1-hour brief outage produces 0 ZAR loss because buffers absorb the disruption")
    void testBriefOutageAbsorbedByBuffer() {
        RippleSimulationRequest briefOutage = new RippleSimulationRequest(
                OutageCategory.WATER,
                OutageSeverity.LOCAL_PIPE_BURST,
                -33.9620,
                22.4620,
                "Ward 18",
                "Minor valve leak",
                1.0, // 1 hour expected outage
                2.5,
                4.0
        );

        RippleSimulationResponse response = rippleEngine.evaluateImpact(briefOutage);

        // Economic loss should be minimal/zero because 1h <= facility buffers
        assertThat(response.totalProjectedEconomicLossZar()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Cascade Duration: Severe 12-hour outage exhausts all buffers and calculates maximum daily risk")
    void testSevereProlongedOutageCalculatesMaxDailyLoss() {
        RippleSimulationRequest severeOutage = new RippleSimulationRequest(
                OutageCategory.WATER,
                OutageSeverity.CRITICAL_MAIN_RUPTURE,
                -33.9620,
                22.4620,
                "Ward 18",
                "Full trunk failure across George",
                12.0, // 12-hour outage
                3.0,
                4.0
        );

        RippleSimulationResponse response = rippleEngine.evaluateImpact(severeOutage);

        // Loss must be substantial (> R20,000 across local facilities)
        assertThat(response.totalProjectedEconomicLossZar()).isGreaterThan(20000.0);
        assertThat(response.criticalRiskCount()).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("Remote Incident: Zero affected facilities returns empty list gracefully")
    void testRemoteIncidentWithZeroThreatenedFacilities() {
        RippleSimulationRequest remoteOutage = new RippleSimulationRequest(
                OutageCategory.WATER,
                OutageSeverity.LOCAL_PIPE_BURST,
                -33.1000, // 90 km into the Swartberg mountains
                21.5000,
                "Uninhabited Ward",
                "Mountain pipe rupture",
                6.0,
                1.0,
                2.0
        );

        RippleSimulationResponse response = rippleEngine.evaluateImpact(remoteOutage);

        assertThat(response.totalSecondaryRisks()).isEqualTo(0);
        assertThat(response.threatenedFacilities()).isEmpty();
        assertThat(response.matchedMutualAid()).isEmpty();
        assertThat(response.totalProjectedEconomicLossZar()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Performance Benchmark: 100 consecutive spatial cascade queries execute in < 200ms")
    void testPerformanceBenchmark100Simulations() {
        RippleSimulationRequest request = new RippleSimulationRequest(
                OutageCategory.WATER,
                OutageSeverity.CRITICAL_MAIN_RUPTURE,
                -33.9620,
                22.4620,
                "Ward 18",
                "Benchmark run",
                6.0,
                2.5,
                4.0
        );

        long start = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            RippleSimulationResponse response = rippleEngine.evaluateImpact(request);
            assertThat(response).isNotNull();
        }
        long duration = System.currentTimeMillis() - start;

        assertThat(duration)
                .as("100 spatial cascade evaluations must execute in < 200ms for hackathon live demo")
                .isLessThan(200);
    }
}

