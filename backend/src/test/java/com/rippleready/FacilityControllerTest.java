package com.rippleready;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rippleready.model.DependentFacility;
import com.rippleready.model.FacilityType;
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
class FacilityControllerTest {

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
    @DisplayName("GET /api/facilities should list all registered facilities")
    void testListFacilities() throws Exception {
        mockMvc.perform(get("/api/facilities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(5))))
                .andExpect(jsonPath("$[0].name", notNullValue()))
                .andExpect(jsonPath("$[0].type", notNullValue()));
    }

    @Test
    @DisplayName("GET /api/facilities/{id} should return specific facility details")
    void testGetFacilityById() throws Exception {
        mockMvc.perform(get("/api/facilities/fac-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("fac-1")))
                .andExpect(jsonPath("$.name", containsString("Little Stars")))
                .andExpect(jsonPath("$.beneficiaryCount", is(40)));
    }

    @Test
    @DisplayName("GET /api/facilities/{id} with unknown ID should return 404 Not Found")
    void testGetFacilityNotFound() throws Exception {
        mockMvc.perform(get("/api/facilities/fac-non-existent-999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/facilities should register a new community anchor node")
    void testRegisterNewFacility() throws Exception {
        DependentFacility newCrèche = new DependentFacility(
                null,
                "Sunshine Early Childhood Centre",
                FacilityType.CRECHE,
                -33.9645, 22.4630,
                "12 Sandkraal Rd, George",
                "Teacher Nomsa",
                "+27 82 555 9182",
                35,
                2,
                "Potable water for midday infant formula preparation",
                2800.0,
                true
        );

        mockMvc.perform(post("/api/facilities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCrèche)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", startsWith("fac-")))
                .andExpect(jsonPath("$.name", is("Sunshine Early Childhood Centre")))
                .andExpect(jsonPath("$.beneficiaryCount", is(35)))
                .andExpect(jsonPath("$.status", is("STABLE")));
    }
}
