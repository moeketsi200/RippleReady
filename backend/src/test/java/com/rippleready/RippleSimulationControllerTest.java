package com.rippleready;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rippleready.model.OutageCategory;
import com.rippleready.model.OutageSeverity;
import com.rippleready.model.PledgeRequest;
import com.rippleready.model.RippleSimulationRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RippleSimulationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /healthz should return 200 OK")
    void testHealthCheck() throws Exception {
        mockMvc.perform(get("/healthz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("UP")))
                .andExpect(jsonPath("$.service", containsString("RippleReady")));
    }

    @Test
    @DisplayName("GET /api/ripple/demo-scenario should return pre-configured incident")
    void testGetDemoScenario() throws Exception {
        mockMvc.perform(get("/api/ripple/demo-scenario"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category", is("WATER")))
                .andExpect(jsonPath("$.wardNumber", is("Ward 18")));
    }

    @Test
    @DisplayName("POST /api/ripple/simulate should compute ripple consequences in real time")
    void testSimulateCascadeEndpoint() throws Exception {
        RippleSimulationRequest request = new RippleSimulationRequest(
                OutageCategory.WATER,
                OutageSeverity.CRITICAL_MAIN_RUPTURE,
                -33.9620,
                22.4620,
                "Ward 18",
                "York Street burst",
                5.0,
                2.5,
                4.0
        );

        mockMvc.perform(post("/api/ripple/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSecondaryRisks", greaterThan(0)))
                .andExpect(jsonPath("$.threatenedFacilities", not(empty())))
                .andExpect(jsonPath("$.matchedMutualAid", not(empty())))
                .andExpect(jsonPath("$.executionTimeMs", lessThan(1000)));
    }

    @Test
    @DisplayName("POST /api/resources/pledge should allocate capacity and record the pledge")
    void testPledgeEndpoint() throws Exception {
        PledgeRequest pledge = new PledgeRequest(
                "res-1",
                "fac-1",
                100.0,
                "+27 82 555 0192",
                "Emergency borehole allocation for crèche toddlers"
        );

        mockMvc.perform(post("/api/resources/pledge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pledge)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.allocatedUnits", is(100.0)))
                .andExpect(jsonPath("$.status", is("DISPATCHED")));
    }

    @Test
    @DisplayName("GET /api/impact/summary should return live impact dashboard metrics")
    void testImpactSummary() throws Exception {
        mockMvc.perform(get("/api/impact/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalBeneficiariesProtected", greaterThan(0)))
                .andExpect(jsonPath("$.pilotWard", containsString("Ward 18")));
    }
}

