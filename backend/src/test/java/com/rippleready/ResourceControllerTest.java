package com.rippleready;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rippleready.model.MutualAidResource;
import com.rippleready.model.PledgeRequest;
import com.rippleready.model.ResourceType;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ResourceControllerTest {

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
    @DisplayName("GET /api/resources should list all registered capacity providers")
    void testListResources() throws Exception {
        mockMvc.perform(get("/api/resources"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(3))))
                .andExpect(jsonPath("$[0].providerName", notNullValue()))
                .andExpect(jsonPath("$[0].totalCapacity", greaterThan(0.0)));
    }

    @Test
    @DisplayName("POST /api/resources should register a new community mutual aid reserve")
    void testRegisterNewResource() throws Exception {
        MutualAidResource newReserve = new MutualAidResource(
                null,
                "Pacaltsdorp Community Centre Tank",
                ResourceType.JOJO_POTABLE,
                -33.9720, 22.4510,
                "Mission St, Pacaltsdorp",
                "Pastor Karel",
                "+27 83 555 3119",
                5000.0,
                5000.0,
                "Litres",
                true,
                false
        );

        mockMvc.perform(post("/api/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newReserve)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", startsWith("res-")))
                .andExpect(jsonPath("$.providerName", is("Pacaltsdorp Community Centre Tank")))
                .andExpect(jsonPath("$.availableCapacity", is(5000.0)));
    }

    @Test
    @DisplayName("GET /api/resources/match should return only providers with capacity >= requiredVolume")
    void testFindMatchingResourcesWithVolumeConstraint() throws Exception {
        mockMvc.perform(get("/api/resources/match")
                        .param("lat", "-33.9630")
                        .param("lon", "22.4610")
                        .param("radiusKm", "4.0")
                        .param("requiredVolume", "1200.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$[0].resource.availableCapacity", greaterThanOrEqualTo(1200.0)));
    }

    @Test
    @DisplayName("GET /api/resources/pledges should list all created dispatch pledges")
    void testListPledgesAfterCreation() throws Exception {
        PledgeRequest pledge = new PledgeRequest(
                "res-1",
                "fac-1",
                250.0,
                "+27 82 555 0192",
                "Test pledge for listing"
        );

        mockMvc.perform(post("/api/resources/pledge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pledge)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/resources/pledges"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].allocatedUnits", is(250.0)))
                .andExpect(jsonPath("$[0].status", is("DISPATCHED")));
    }
}
