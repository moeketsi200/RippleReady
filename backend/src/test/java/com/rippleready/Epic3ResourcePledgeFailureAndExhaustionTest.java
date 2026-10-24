package com.rippleready;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rippleready.model.MutualAidResource;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class Epic3ResourcePledgeFailureAndExhaustionTest {

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
    @DisplayName("Pledge Failure: Requesting more capacity than available returns HTTP 409 Conflict")
    void testInsufficientCapacityThrowsConflict() throws Exception {
        // Hendricks Hardware has 1500L available
        PledgeRequest overAllocation = new PledgeRequest(
                "res-1",
                "fac-1",
                2500.0, // Exceeds 1500L
                "+27 82 555 0192",
                "+27 82 555 9901",
                "TOKEN-OVER",
                "Excessive allocation attempt"
        );

        mockMvc.perform(post("/api/resources/pledge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(overAllocation)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("Insufficient capacity")));
    }

    @Test
    @DisplayName("Pledge Exhaustion: Exact capacity allocation drains balance to 0, subsequent pledge fails")
    void testExactCapacityExhaustion() throws Exception {
        // First allocation: Take all 1500L
        PledgeRequest takeAll = new PledgeRequest(
                "res-1",
                "fac-1",
                1500.0,
                "+27 82 555 0192",
                "+27 82 555 9901",
                "TOKEN-ALL",
                "Full reserve allocation"
        );

        mockMvc.perform(post("/api/resources/pledge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(takeAll)))
                .andExpect(status().isOk());

        // Provider capacity is now exactly 0.0
        MutualAidResource provider = dataStore.getResourceById("res-1").get();
        assertThat(provider.availableCapacity()).isEqualTo(0.0);

        // Second allocation: even 10L must now fail
        PledgeRequest subsequentRequest = new PledgeRequest(
                "res-1",
                "fac-2",
                10.0,
                "+27 82 555 0192",
                "+27 82 555 9901",
                "TOKEN-FAIL",
                "Should fail due to zero balance"
        );

        mockMvc.perform(post("/api/resources/pledge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(subsequentRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("Insufficient capacity")));
    }

    @Test
    @DisplayName("Pledge Validation: Non-existent resource ID returns HTTP 400 Bad Request")
    void testNonExistentResourceId() throws Exception {
        PledgeRequest invalidResource = new PledgeRequest(
                "res-invalid-999",
                "fac-1",
                100.0,
                "+27 82 555 0192",
                "Invalid resource"
        );

        mockMvc.perform(post("/api/resources/pledge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidResource)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Resource not found")));
    }

    @Test
    @DisplayName("Pledge Validation: Non-existent facility ID returns HTTP 400 Bad Request")
    void testNonExistentFacilityId() throws Exception {
        PledgeRequest invalidFacility = new PledgeRequest(
                "res-1",
                "fac-invalid-999",
                100.0,
                "+27 82 555 0192",
                "Invalid facility"
        );

        mockMvc.perform(post("/api/resources/pledge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidFacility)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Facility not found")));
    }

    @Test
    @DisplayName("Pledge Validation: Negative or zero allocation units rejected with HTTP 400")
    void testNegativeAllocationRejected() throws Exception {
        PledgeRequest negativePledge = new PledgeRequest(
                "res-1",
                "fac-1",
                -100.0,
                "+27 82 555 0192",
                "Negative units"
        );

        mockMvc.perform(post("/api/resources/pledge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(negativePledge)))
                .andExpect(status().isBadRequest());
    }
}

