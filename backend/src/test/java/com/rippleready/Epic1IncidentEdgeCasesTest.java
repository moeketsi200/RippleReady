package com.rippleready;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rippleready.model.IncidentRecord;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class Epic1IncidentEdgeCasesTest {

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
    @DisplayName("Edge Case: Exact coordinate boundaries (-90, +90, -180, +180) must be accepted")
    void testExactCoordinateBoundariesAccepted() throws Exception {
        IncidentReportRequest validBoundaryReport = new IncidentReportRequest(
                "rep-boundary",
                "Water",
                -90.0, // South Pole
                180.0, // International Date Line
                "CRITICAL",
                "Ward Remote",
                "Boundary coordinate test"
        );

        mockMvc.perform(post("/api/incidents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validBoundaryReport)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.latitude", is(-90.0)))
                .andExpect(jsonPath("$.longitude", is(180.0)));
    }

    @Test
    @DisplayName("Edge Case: Coordinate just beyond boundary (+90.0001) must be rejected with HTTP 422")
    void testCoordinateJustBeyondBoundaryRejected() throws Exception {
        IncidentReportRequest invalidReport = new IncidentReportRequest(
                "rep-boundary",
                "Water",
                90.0001, // Invalid
                22.4610,
                "CRITICAL",
                "Ward 18",
                "Boundary overflow"
        );

        mockMvc.perform(post("/api/incidents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidReport)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail", is("Coordinates out of permissible range")));
    }

    @Test
    @DisplayName("Edge Case: Null coordinates must be rejected with HTTP 422")
    void testNullCoordinatesRejected() throws Exception {
        IncidentReportRequest nullCoords = new IncidentReportRequest(
                "rep-null",
                "Water",
                null,
                null,
                "CRITICAL",
                "Ward 18",
                "Missing coordinates"
        );

        mockMvc.perform(post("/api/incidents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nullCoords)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail", is("Coordinates out of permissible range")));
    }

    @Test
    @DisplayName("Edge Case: Different category at same coordinates does NOT deduplicate (Water vs Electricity)")
    void testDifferentCategoryDoesNotDeduplicate() throws Exception {
        // Report 1: Water disruption
        IncidentReportRequest waterReport = new IncidentReportRequest(
                "rep-1",
                "Water",
                -33.9630,
                22.4610,
                "CRITICAL",
                "Ward 18, George",
                "Water main burst"
        );

        mockMvc.perform(post("/api/incidents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(waterReport)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is("inc-001")));

        // Report 2: Electricity failure at exact same location
        IncidentReportRequest powerReport = new IncidentReportRequest(
                "rep-2",
                "Electricity",
                -33.9630,
                22.4610,
                "CRITICAL",
                "Ward 18, George",
                "Transformer blown"
        );

        mockMvc.perform(post("/api/incidents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(powerReport)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is("inc-002"))) // New incident spawned!
                .andExpect(jsonPath("$.isDuplicate", is(false)));

        assertThat(dataStore.getAllIncidents()).hasSize(2);
    }

    @Test
    @DisplayName("Edge Case: Multiple citizens confirm same incident, accumulating confidence score")
    void testMultipleConfirmationVotesAccumulate() throws Exception {
        IncidentReportRequest initialReport = new IncidentReportRequest(
                "rep-george-04",
                "Water",
                -33.9630,
                22.4610,
                "CRITICAL",
                "Ward 18, George",
                "Initial report"
        );

        mockMvc.perform(post("/api/incidents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(initialReport)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.confidenceScore", is(100)));

        // Citizen 1 confirms (~50m away)
        IncidentReportRequest citizen1 = new IncidentReportRequest(
                "citizen-1", "Water", -33.9632, 22.4612, "CRITICAL", "Ward 18", "Confirming burst"
        );
        mockMvc.perform(post("/api/incidents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(citizen1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confidenceScore", is(125)))
                .andExpect(jsonPath("$.confirmationVotes", is(2)));

        // Citizen 2 confirms (~100m away)
        IncidentReportRequest citizen2 = new IncidentReportRequest(
                "citizen-2", "Water", -33.9634, 22.4613, "CRITICAL", "Ward 18", "No water on 2nd Ave"
        );
        mockMvc.perform(post("/api/incidents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(citizen2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confidenceScore", is(150)))
                .andExpect(jsonPath("$.confirmationVotes", is(3)));

        // Only ONE incident record exists in dataStore
        assertThat(dataStore.getAllIncidents()).hasSize(1);
        IncidentRecord finalRecord = dataStore.getAllIncidents().get(0);
        assertThat(finalRecord.confidenceScore()).isEqualTo(150);
        assertThat(finalRecord.confirmationVotes()).isEqualTo(3);
    }
}

