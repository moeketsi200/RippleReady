package com.rippleready;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rippleready.model.PledgeRequest;
import com.rippleready.service.DataStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuditAndImpactControllersTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DataStore dataStore;

    @BeforeEach
    void setUp() {
        dataStore.reset();
    }

    @Test
    @DisplayName("GET /api/impact/summary should return complete quantified resilience metrics")
    void testImpactSummaryMetrics() throws Exception {
        mockMvc.perform(get("/api/impact/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pilotWard", containsString("Ward 18, George")))
                .andExpect(jsonPath("$.secondaryCrisesAverted", greaterThanOrEqualTo(5)))
                .andExpect(jsonPath("$.totalBeneficiariesProtected", greaterThan(0)))
                .andExpect(jsonPath("$.totalEconomicLossAvertedZar", greaterThan(15000.0)))
                .andExpect(jsonPath("$.averageResponseTimeMinutes", is(18.4)));
    }

    @Test
    @DisplayName("GET /api/audit/ledger should return audit events with cryptographic SHA-256 hashes")
    void testAuditLedgerEndpoint() throws Exception {
        // Trigger a pledge to populate audit ledger
        PledgeRequest pledge = new PledgeRequest(
                "res-1",
                "fac-1",
                150.0,
                "+27 82 555 0192",
                "Audit ledger test pledge"
        );

        mockMvc.perform(post("/api/resources/pledge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pledge)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/audit/ledger"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$[0].eventType", is("PLEDGE_RESERVATION_LOCKED")))
                .andExpect(jsonPath("$[0].auditHash", matchesRegex("^[a-f0-9]{64}$")));
    }

    @Test
    @DisplayName("POST /api/fnb/disburse-fuel-grant should successfully trigger instant pay disbursement via REST")
    void testFnbDisburseFuelGrantRestEndpoint() throws Exception {
        Map<String, Object> payload = Map.of(
                "driverMobile", "+27 82 999 4433",
                "roundTripKm", 4.0,
                "litersDelivered", 1000.0,
                "verificationToken", "TOKEN-MANUAL-REST"
        );

        mockMvc.perform(post("/api/fnb/disburse-fuel-grant")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("SUCCESS")))
                .andExpect(jsonPath("$.amountZar", is(150.0)))
                .andExpect(jsonPath("$.recipientMobile", is("+27 82 999 4433")))
                .andExpect(jsonPath("$.transactionReference", startsWith("FNB-PAY-")));
    }
}
