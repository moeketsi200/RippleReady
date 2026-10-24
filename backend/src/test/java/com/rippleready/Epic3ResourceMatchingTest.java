package com.rippleready;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rippleready.model.MatchedResource;
import com.rippleready.model.MutualAidResource;
import com.rippleready.model.PledgeRecord;
import com.rippleready.model.PledgeRequest;
import com.rippleready.service.DataStore;
import com.rippleready.service.RippleEngineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class Epic3ResourceMatchingTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DataStore dataStore;

    @Autowired
    private RippleEngineService rippleEngine;

    @BeforeEach
    void setUp() {
        dataStore.reset();
    }

    @Test
    @DisplayName("US-3.1: Automated matching of borehole and JoJo capacity within 3.0 km ranked ascending by distance")
    void testUS3_1_AutomatedMatchingWithin3km() throws Exception {
        // Query capacity from epicenter (-33.9630, 22.4610) for 500L within 3.0 km
        mockMvc.perform(get("/api/resources/match")
                        .param("lat", "-33.9630")
                        .param("lon", "22.4610")
                        .param("radiusKm", "3.0")
                        .param("requiredVolume", "500.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
                // Verify ordering ascending by distance
                .andExpect(jsonPath("$[0].distanceKm", lessThanOrEqualTo(2.0)))
                .andExpect(jsonPath("$[0].resource.availableCapacity", greaterThanOrEqualTo(500.0)));

        List<MatchedResource> matches = rippleEngine.findMatchingResources(
                -33.9630, 22.4610, 3.0, 500.0
        );

        assertThat(matches.size()).isGreaterThanOrEqualTo(2);
        // Verify strictly ranked ascending by distance
        for (int i = 0; i < matches.size() - 1; i++) {
            assertThat(matches.get(i).distanceKm())
                    .isLessThanOrEqualTo(matches.get(i + 1).distanceKm());
        }
        // Verify available capacity meets or exceeds required 500L
        for (MatchedResource match : matches) {
            assertThat(match.resource().availableCapacity()).isGreaterThanOrEqualTo(500.0);
        }
    }

    @Test
    @DisplayName("US-3.2: Pledging and capacity reservation lock")
    void testUS3_2_PledgingAndCapacityReservationLock() throws Exception {
        // Given resource provider "Hendricks Hardware" with 1500L available
        MutualAidResource initialHardware = dataStore.getResourceById("res-1")
                .orElseThrow(() -> new AssertionError("res-1 Hendricks Hardware not found"));
        assertThat(initialHardware.availableCapacity()).isEqualTo(1500.0);

        // Facility initially in THREATENED state
        assertThat(dataStore.getFacilityById("fac-1").get().status()).isEqualTo("THREATENED");

        // When provider accepts match to deliver 500L to "Little Stars Crèche"
        PledgeRequest pledge = new PledgeRequest(
                "res-1",
                "fac-1",
                500.0,
                "+27 82 555 0192",
                "+27 82 555 9901",
                "TOKEN-789", // Verification token
                "Emergency borehole allocation for crèche toddlers"
        );

        mockMvc.perform(post("/api/resources/pledge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pledge)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.allocatedUnits", is(500.0)))
                .andExpect(jsonPath("$.verificationToken", is("TOKEN-789")))
                .andExpect(jsonPath("$.status", is("DISPATCHED")));

        // Then provider's active available capacity decrements from 1500L to 1000L
        MutualAidResource updatedHardware = dataStore.getResourceById("res-1").get();
        assertThat(updatedHardware.availableCapacity()).isEqualTo(1000.0);

        // And facility status transitions from THREATENED to DISPATCHED
        assertThat(dataStore.getFacilityById("fac-1").get().status()).isEqualTo("DISPATCHED");
    }
}

