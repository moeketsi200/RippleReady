package com.rippleready;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class DatabaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /api/database/status: Returns SQLite healthy status, WAL journal mode, and table counts")
    void testGetDatabaseStatus() throws Exception {
        mockMvc.perform(get("/api/database/status"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("HEALTHY"))
                .andExpect(jsonPath("$.engine").value("SQLite 3"))
                .andExpect(jsonPath("$.journalMode").value("wal"))
                .andExpect(jsonPath("$.facilityCount").isNumber())
                .andExpect(jsonPath("$.resourceCount").isNumber());
    }

    @Test
    @DisplayName("POST /api/database/reset: Clears and re-seeds SQLite tables successfully")
    void testResetDatabase() throws Exception {
        mockMvc.perform(post("/api/database/reset"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("HEALTHY"))
                .andExpect(jsonPath("$.facilityCount").value(6))
                .andExpect(jsonPath("$.resourceCount").value(3))
                .andExpect(jsonPath("$.message").exists());
    }
}
