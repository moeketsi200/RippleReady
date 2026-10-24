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

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class Epic4DeliverySecurityAndReplayAttackTest {

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
    @DisplayName("Security: Preventing Replay Attack — Token cannot be redeemed more than once")
    void testReplayAttackPrevention() throws Exception {
        String token = "TOKEN-ONCE-ONLY";

        // Create initial pledge
        PledgeRequest pledge = new PledgeRequest(
                "res-1",
                "fac-1",
                300.0,
                "+27 82 555 0192",
                "+27 82 555 9901",
                token,
                "Single delivery token"
        );
        pledgeService.createPledge(pledge);

        DeliveryVerificationRequest verification = new DeliveryVerificationRequest(
                token,
                "Sis' Thandiwe",
                "300L water received",
                3.5
        );

        // First verification: Must succeed
        mockMvc.perform(post("/api/deliveries/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verification)))
                .andExpect(status().isOk());

        // Second verification with identical token (Replay Attack): Must be rejected!
        mockMvc.perform(post("/api/deliveries/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verification)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("already been verified")));
    }

    @Test
    @DisplayName("Security: Unknown or forged verification token returns HTTP 400 Bad Request")
    void testUnknownVerificationTokenRejected() throws Exception {
        DeliveryVerificationRequest forgedToken = new DeliveryVerificationRequest(
                "FORGED-TOKEN-9999",
                "Impostor",
                "Trying to claim payout without genuine dispatch",
                4.0
        );

        mockMvc.perform(post("/api/deliveries/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(forgedToken)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Invalid or unknown verification token")));
    }

    @Test
    @DisplayName("Validation: Empty or whitespace verification token rejected by @NotBlank")
    void testBlankVerificationTokenRejected() throws Exception {
        DeliveryVerificationRequest blankToken = new DeliveryVerificationRequest(
                "   ",
                "Recipient",
                "Blank token",
                4.0
        );

        mockMvc.perform(post("/api/deliveries/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(blankToken)))
                .andExpect(status().isBadRequest());
    }
}

