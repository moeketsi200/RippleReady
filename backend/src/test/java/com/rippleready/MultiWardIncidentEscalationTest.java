package com.rippleready;

import com.rippleready.model.*;
import com.rippleready.service.DataStore;
import com.rippleready.service.IncidentService;
import com.rippleready.service.RippleEngineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class MultiWardIncidentEscalationTest {

    @Autowired
    private DataStore dataStore;

    @Autowired
    private IncidentService incidentService;

    @Autowired
    private RippleEngineService rippleEngine;

    @BeforeEach
    void setUp() {
        dataStore.reset();
    }

    @Test
    @DisplayName("Multi-Ward: Independent epicenters in Ward 18 and Ward 24 do not cross-pollute local perimeters")
    void testMultiWardIsolation() {
        // Incident A: Ward 18 (Rosemoor/York St junction)
        IncidentRecord ward18Incident = incidentService.reportIncident(new IncidentReportRequest(
                "rep-ward-18",
                "Water",
                -33.9630, 22.4610,
                "CRITICAL",
                "Ward 18, George",
                "Feeder burst"
        ));

        // Incident B: Ward 24 (Pacaltsdorp, ~4.5 km south-west)
        IncidentRecord ward24Incident = incidentService.reportIncident(new IncidentReportRequest(
                "rep-ward-24",
                "Water",
                -33.9850, 22.4350,
                "CRITICAL",
                "Ward 24, Pacaltsdorp",
                "Reservoir valve failure"
        ));

        // Verify two separate incidents created (not deduplicated because > 350m apart)
        assertThat(ward18Incident.id()).isNotEqualTo(ward24Incident.id());
        assertThat(ward18Incident.isDuplicate()).isFalse();
        assertThat(ward24Incident.isDuplicate()).isFalse();

        // Simulate Ward 18 with 2.0 km radius
        RippleSimulationResponse response18 = rippleEngine.evaluateImpact(new RippleSimulationRequest(
                OutageCategory.WATER,
                OutageSeverity.CRITICAL_MAIN_RUPTURE,
                ward18Incident.latitude(),
                ward18Incident.longitude(),
                "Ward 18",
                "Ward 18 outage",
                6.0,
                2.0,
                3.0
        ));

        // All facilities in Ward 18 cascade must be strictly <= 2.0 km from Ward 18 epicenter
        for (ThreatenedFacility tf : response18.threatenedFacilities()) {
            assertThat(tf.distanceKm()).isLessThanOrEqualTo(2.0);
        }
    }

    @Test
    @DisplayName("Multi-Ward: Sourcing query from Ward 24 finds mobile bowser reserves")
    void testMobileBowserCrossWardDisasterRouting() {
        // Query capacity from Ward 24 (-33.9850, 22.4350) with 4.5 km sourcing radius
        List<MatchedResource> matches = rippleEngine.findMatchingResources(
                -33.9850, 22.4350, 4.5, 500.0
        );

        assertThat(matches).isNotEmpty();
        boolean hasBowser = matches.stream()
                .anyMatch(m -> m.resource().resourceType() == ResourceType.MOBILE_BOWSER);
        assertThat(hasBowser).isTrue();
    }

    @Test
    @DisplayName("Multi-Ward: Aggregated municipal disaster impact across concurrent wards")
    void testSimultaneousMultiWardImpactAggregation() {
        List<DependentFacility> allFacilities = dataStore.getAllFacilities();
        int totalBeneficiaries = allFacilities.stream().mapToInt(DependentFacility::beneficiaryCount).sum();
        double totalTurnoverRisk = allFacilities.stream().mapToDouble(DependentFacility::estimatedDailyLossZar).sum();

        // City-wide protection capacity
        assertThat(totalBeneficiaries).isGreaterThan(700);
        assertThat(totalTurnoverRisk).isGreaterThan(50000.0);
    }
}
