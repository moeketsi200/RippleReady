package com.rippleready;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rippleready.model.IncidentReportRequest;
import com.rippleready.service.DataStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class Epic1IncidentIngestionTest {

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
    @DisplayName("US-1.1: Successful creation of a verified water outage report")
    void testUS1_1_SuccessfulCreationOfVerifiedWaterOutageReport() throws Exception {
        IncidentReportRequest request = new IncidentReportRequest(
                "rep-george-04",
                "Water",
                -33.9630,
                22.4610,
                "CRITICAL",
                "Ward 18, George",
                "Primary municipal water line burst near York Street"
        );

        mockMvc.perform(post("/api/incidents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is("inc-001")))
                .andExpect(jsonPath("$.status", is("VERIFIED")))
                .andExpect(jsonPath("$.impactRadiusKm", is(2.0)))
                .andExpect(jsonPath("$.ward", is("Ward 18, George")))
                .andExpect(jsonPath("$.confidenceScore", is(100)))
                .andExpect(jsonPath("$.isDuplicate", is(false)));
    }

    @Test
    @DisplayName("US-1.2: Reject invalid or out-of-bounds geographic coordinates (Latitude 95.0)")
    void testUS1_2_RejectOutOfBoundsLatitude() throws Exception {
        IncidentReportRequest request = new IncidentReportRequest(
                "user-test-01",
                "Water",
                95.0000, // Invalid latitude > 90
                22.4610,
                "CRITICAL",
                "Ward 18, George",
                "Out-of-bounds test"
        );

        mockMvc.perform(post("/api/incidents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail", is("Coordinates out of permissible range")));
    }

    @Test
    @DisplayName("US-1.2: Reject invalid or out-of-bounds geographic coordinates (Longitude -190.0)")
    void testUS1_2_RejectOutOfBoundsLongitude() throws Exception {
        IncidentReportRequest request = new IncidentReportRequest(
                "user-test-02",
                "Water",
                -33.9630,
                -190.0000, // Invalid longitude < -180
                "CRITICAL",
                "Ward 18, George",
                "Out-of-bounds test"
        );

        mockMvc.perform(post("/api/incidents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail", is("Coordinates out of permissible range")));
    }

    @Test
    @DisplayName("US-1.3: Duplicate report deduplication within radius and time window")
    void testUS1_3_DuplicateReportDeduplication() throws Exception {
        // First report at (-33.9630, 22.4610)
        IncidentReportRequest firstReport = new IncidentReportRequest(
                "rep-george-04",
                "Water",
                -33.9630,
                22.4610,
                "CRITICAL",
                "Ward 18, George",
                "York Street burst"
        );

        mockMvc.perform(post("/api/incidents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstReport)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is("inc-001")))
                .andExpect(jsonPath("$.confidenceScore", is(100)));

        // Second report ~70 meters away at (-33.9635, 22.4615) within 15 minutes
        IncidentReportRequest duplicateReport = new IncidentReportRequest(
                "citizen-george-12",
                "Water",
                -33.9635,
                22.4615,
                "CRITICAL",
                "Ward 18, George",
                "Water gushing down York St"
        );

        mockMvc.perform(post("/api/incidents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateReport)))
                .andExpect(status().isOk()) // Confirmed existing incident
                .andExpect(jsonPath("$.isDuplicate", is(true)))
                .andExpect(jsonPath("$.linkedIncidentId", is("inc-001")))
                .andExpect(jsonPath("$.confidenceScore", greaterThan(100))); // Incremented confidence score
    }
}

