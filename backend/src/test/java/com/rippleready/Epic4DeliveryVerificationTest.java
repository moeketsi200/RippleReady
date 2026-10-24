package com.rippleready;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rippleready.model.DeliveryVerificationRequest;
import com.rippleready.model.PledgeRequest;
import com.rippleready.service.DataStore;
import com.rippleready.service.PledgeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class Epic4DeliveryVerificationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DataStore dataStore;

    @Autowired
    private PledgeService pledgeService;

    @BeforeEach
    void setUp() {
        dataStore.reset();
    }

    @Test
    @DisplayName("US-4.1: Handshake confirmation closes the incident loop and turns node green")
    void testUS4_1_HandshakeConfirmationClosesIncidentLoop() throws Exception {
        // Setup in-transit delivery with token "TOKEN-789"
        PledgeRequest pledge = new PledgeRequest(
                "res-1",
                "fac-1",
                500.0,
                "+27 82 555 0192",
                "+27 82 555 9901",
                "TOKEN-789",
                "Immediate water delivery to Little Stars Crèche"
        );
        pledgeService.createPledge(pledge);

        // Verify facility is currently DISPATCHED
        assertThat(dataStore.getFacilityById("fac-1").get().status()).isEqualTo("DISPATCHED");

        // When recipient confirms delivery using "TOKEN-789"
        DeliveryVerificationRequest verification = new DeliveryVerificationRequest(
                "TOKEN-789",
                "Sis' Thandiwe (Crèche Principal)",
                "500L clean water received and pumped into internal tanks",
                4.0
        );

        mockMvc.perform(post("/api/deliveries/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verification)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.facilityStatus", is("RESOLVED")))
                .andExpect(jsonPath("$.dashboardNodeColor", is("GREEN")))
                .andExpect(jsonPath("$.litersDelivered", is(500.0)))
                .andExpect(jsonPath("$.beneficiariesProtected", is(40)));

        // Facility status transitions to RESOLVED in database
        assertThat(dataStore.getFacilityById("fac-1").get().status()).isEqualTo("RESOLVED");

        // And impact ledger increments distributed liters & protected children
        mockMvc.perform(get("/api/impact/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalWaterAllocatedLitres", is(500.0)))
                .andExpect(jsonPath("$.totalBeneficiariesProtected", is(40)));
    }
}

