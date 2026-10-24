package com.rippleready;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rippleready.model.*;
import com.rippleready.service.AuditLedgerService;
import com.rippleready.service.DataStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class HackathonEndToEndWinningFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DataStore dataStore;

    @Autowired
    private AuditLedgerService auditLedgerService;

    @BeforeEach
    void setUp() {
        dataStore.reset();
    }

    @Test
    @DisplayName("Complete Hackathon Winning Flow: Epic 1 to Epic 5 full lifecycle simulation")
    void testCompleteHackathonWorkflow() throws Exception {
        // =========================================================================
        // STEP 1: Epic 1 - Ingest verified water main rupture in Ward 18, George
        // =========================================================================
        IncidentReportRequest incident = new IncidentReportRequest(
                "rep-george-04",
                "Water",
                -33.9630,
                22.4610,
                "CRITICAL",
                "Ward 18, George",
                "Major York Street primary distribution feeder rupture"
        );

        MvcResult incidentResult = mockMvc.perform(post("/api/incidents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(incident)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is("inc-001")))
                .andExpect(jsonPath("$.status", is("VERIFIED")))
                .andExpect(jsonPath("$.impactRadiusKm", is(2.0)))
                .andReturn();

        IncidentRecord recordedIncident = objectMapper.readValue(
                incidentResult.getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8), IncidentRecord.class
        );

        // =========================================================================
        // STEP 2: Epic 2 - Ripple Engine detects downstream vulnerable anchors
        // =========================================================================
        RippleSimulationRequest simulationReq = new RippleSimulationRequest(
                OutageCategory.WATER,
                OutageSeverity.CRITICAL_MAIN_RUPTURE,
                recordedIncident.latitude(),
                recordedIncident.longitude(),
                recordedIncident.ward(),
                "York Street primary burst",
                6.0,
                2.0, // 2.0 km impact zone
                3.5
        );

        MvcResult simulationResult = mockMvc.perform(post("/api/ripple/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(simulationReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSecondaryRisks", greaterThanOrEqualTo(3)))
                .andExpect(jsonPath("$.executionTimeMs", lessThan(500)))
                .andReturn();

        RippleSimulationResponse simulation = objectMapper.readValue(
                simulationResult.getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8), RippleSimulationResponse.class
        );

        // Check crèche is marked CRITICAL (Life & Care priority)
        ThreatenedFacility topThreat = simulation.threatenedFacilities().get(0);
        assertThat(topThreat.urgencyLevel()).isEqualTo(UrgencyLevel.CRITICAL);
        assertThat(topThreat.facility().name()).contains("Little Stars");

        // =========================================================================
        // STEP 3: Epic 3 - Sourcing & Capacity Matching within 3.0 km
        // =========================================================================
        mockMvc.perform(get("/api/resources/match")
                        .param("lat", String.valueOf(topThreat.facility().latitude()))
                        .param("lon", String.valueOf(topThreat.facility().longitude()))
                        .param("radiusKm", "3.0")
                        .param("requiredVolume", "500.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].distanceKm", lessThanOrEqualTo(2.0)));

        // Provider locks in pledge with token
        PledgeRequest pledgeReq = new PledgeRequest(
                "res-1",
                topThreat.facility().id(),
                500.0,
                "+27 82 555 0192",
                "+27 82 555 9901",
                "TOKEN-WIN-01",
                "Hackathon live verification delivery"
        );

        mockMvc.perform(post("/api/resources/pledge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pledgeReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("DISPATCHED")))
                .andExpect(jsonPath("$.verificationToken", is("TOKEN-WIN-01")));

        // Facility status transitions to DISPATCHED
        assertThat(dataStore.getFacilityById(topThreat.facility().id()).get().status())
                .isEqualTo("DISPATCHED");

        // =========================================================================
        // STEP 4 & 5: Epic 4 Handshake Verification + Epic 5 FNB Instant Pay Payout
        // =========================================================================
        DeliveryVerificationRequest verifyReq = new DeliveryVerificationRequest(
                "TOKEN-WIN-01",
                "Sis' Thandiwe (Crèche Principal)",
                "500L clean drinking water received and sealed",
                4.0 // 4 km round trip
        );

        mockMvc.perform(post("/api/deliveries/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.facilityStatus", is("RESOLVED")))
                .andExpect(jsonPath("$.dashboardNodeColor", is("GREEN")))
                .andExpect(jsonPath("$.fnbDisbursement.amountZar", is(150.0)))
                .andExpect(jsonPath("$.fnbDisbursement.status", is("SUCCESS")))
                .andExpect(jsonPath("$.fnbDisbursement.transactionReference", startsWith("FNB-PAY-")));

        // Final facility status verified as RESOLVED
        assertThat(dataStore.getFacilityById(topThreat.facility().id()).get().status())
                .isEqualTo("RESOLVED");

        // Audit trail cryptographically logged
        List<AuditLedgerEntry> auditTrail = auditLedgerService.getAuditTrail();
        assertThat(auditTrail.size()).isGreaterThanOrEqualTo(3);
        assertThat(auditTrail.get(auditTrail.size() - 1).auditHash()).isNotBlank();
    }
}
