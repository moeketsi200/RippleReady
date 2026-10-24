package com.rippleready;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rippleready.model.DeliveryVerificationRequest;
import com.rippleready.model.PledgeRequest;
import com.rippleready.service.AuditLedgerService;
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
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class Epic5FnbMicroEscrowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DataStore dataStore;

    @Autowired
    private PledgeService pledgeService;

    @Autowired
    private AuditLedgerService auditLedgerService;

    @BeforeEach
    void setUp() {
        dataStore.reset();
    }

    @Test
    @DisplayName("US-5.1: Automated transport micro-grant payout via FNB Instant Pay API upon delivery handshake")
    void testUS5_1_AutomatedTransportMicroGrantPayoutViaFnbApi() throws Exception {
        // Given a confirmed delivery of 1,000L over a 4 km round trip with registered driver mobile
        String driverMobile = "+27 82 555 4499";
        String token = "TOKEN-FNB-99";

        PledgeRequest pledge = new PledgeRequest(
                "res-1",
                "fac-2", // Mama K's Eatery
                1000.0,
                "+27 82 555 0192",
                driverMobile,
                token,
                "Emergency sanitation run for township eatery"
        );
        pledgeService.createPledge(pledge);

        // When proof of delivery is verified with 4.0 km round trip
        DeliveryVerificationRequest verification = new DeliveryVerificationRequest(
                token,
                "Mama Khanyisa",
                "1000L borehole water pumped into kitchen barrels",
                4.0
        );

        mockMvc.perform(post("/api/deliveries/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verification)))
                .andExpect(status().isOk())
                // Then platform triggers automated payment call to FNB Instant Pay mock API
                .andExpect(jsonPath("$.fnbDisbursement.paymentRail", containsString("FNB Instant Pay")))
                // And dispatches R150 fuel stipend to driver's registered mobile number
                .andExpect(jsonPath("$.fnbDisbursement.amountZar", is(150.0)))
                .andExpect(jsonPath("$.fnbDisbursement.recipientMobile", is(driverMobile)))
                .andExpect(jsonPath("$.fnbDisbursement.status", is("SUCCESS")))
                .andExpect(jsonPath("$.fnbDisbursement.transactionReference", startsWith("FNB-PAY-")));

        // And records transaction reference on the tamper-evident auditable ledger
        mockMvc.perform(get("/api/audit/ledger"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())))
                .andExpect(jsonPath("$[-1:].fnbTxnRef", notNullValue()))
                .andExpect(jsonPath("$[-1:].auditHash", notNullValue()));

        assertThat(auditLedgerService.getAuditTrail()).isNotEmpty();
        boolean foundFnbEntry = auditLedgerService.getAuditTrail().stream()
                .anyMatch(entry -> entry.eventType().equals("FNB_MICRO_GRANT_DISBURSED")
                        && entry.fnbTxnRef() != null && entry.fnbTxnRef().startsWith("FNB-PAY-"));
        assertThat(foundFnbEntry).isTrue();
    }

    @Test
    @DisplayName("US-5.1 Extended: Long-distance transit (10km round trip) scales fuel grant from R150 to R225")
    void testExtendedDistanceFuelGrantScaling() throws Exception {
        String token = "TOKEN-FNB-DIST-10KM";
        PledgeRequest pledge = new PledgeRequest(
                "res-1",
                "fac-1",
                500.0,
                "+27 82 555 0192",
                "+27 82 555 3322",
                token,
                "Long distance water bowser delivery"
        );
        pledgeService.createPledge(pledge);

        // 10.0 km round trip: R150 base + (5km * R15) = R225.0
        DeliveryVerificationRequest verification = new DeliveryVerificationRequest(
                token,
                "Sis' Thandiwe",
                "Received bowser",
                10.0
        );

        mockMvc.perform(post("/api/deliveries/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verification)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fnbDisbursement.amountZar", is(225.0)))
                .andExpect(jsonPath("$.fnbDisbursement.status", is("SUCCESS")));
    }
}

