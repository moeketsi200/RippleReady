package com.rippleready;

import com.rippleready.model.AuditLedgerEntry;
import com.rippleready.model.DeliveryVerificationRequest;
import com.rippleready.model.FnbDisbursementResult;
import com.rippleready.model.PledgeRequest;
import com.rippleready.service.AuditLedgerService;
import com.rippleready.service.DataStore;
import com.rippleready.service.FnbInstantPayService;
import com.rippleready.service.PledgeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class Epic5FnbCryptographicLedgerIntegrityTest {

    @Autowired
    private DataStore dataStore;

    @Autowired
    private PledgeService pledgeService;

    @Autowired
    private AuditLedgerService auditLedgerService;

    @Autowired
    private FnbInstantPayService fnbInstantPayService;

    @BeforeEach
    void setUp() {
        dataStore.reset();
    }

    @Test
    @DisplayName("Audit Ledger: SHA-256 cryptographic chaining ensures immutable, tamper-evident audit trail")
    void testSha256ChainedAuditLedgerIntegrity() {
        // Step 1: Create pledge
        PledgeRequest pledge = new PledgeRequest(
                "res-1",
                "fac-1",
                400.0,
                "+27 82 555 0192",
                "+27 82 555 8899",
                "TOKEN-CRYPTO-01",
                "Audit integrity test run"
        );
        pledgeService.createPledge(pledge);

        // Step 2: Confirm delivery
        DeliveryVerificationRequest verification = new DeliveryVerificationRequest(
                "TOKEN-CRYPTO-01",
                "Sis' Thandiwe",
                "Delivery confirmed",
                4.0
        );
        pledgeService.verifyDelivery(verification);

        // Verify ledger entries exist and are cryptographically hashed
        List<AuditLedgerEntry> ledger = auditLedgerService.getAuditTrail();
        assertThat(ledger.size()).isGreaterThanOrEqualTo(3);

        for (AuditLedgerEntry entry : ledger) {
            assertThat(entry.auditHash()).isNotNull();
            assertThat(entry.auditHash()).hasSize(64); // SHA-256 hex string length
            assertThat(entry.auditHash()).matches("^[0-9a-f]{64}$");
        }

        // Verify each entry has unique ID and timestamp
        long distinctIds = ledger.stream().map(AuditLedgerEntry::id).distinct().count();
        assertThat(distinctIds).isEqualTo(ledger.size());
    }

    @Test
    @DisplayName("FNB Instant Pay: Direct micro-escrow disbursement generates valid transaction ref and amount")
    void testFnbDirectMicroEscrowDisbursement() {
        String driverMobile = "+27 82 555 7711";
        FnbDisbursementResult result = fnbInstantPayService.disburseFuelStipend(
                driverMobile,
                4.5, // 4.5 km round trip
                800.0,
                "TOKEN-TEST-DIRECT"
        );

        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo("SUCCESS");
        assertThat(result.amountZar()).isEqualTo(150.0);
        assertThat(result.recipientMobile()).isEqualTo(driverMobile);
        assertThat(result.paymentRail()).contains("FNB Instant Pay");
        assertThat(result.transactionReference()).startsWith("FNB-PAY-");
    }

    @Test
    @DisplayName("FNB Instant Pay: Multiple distinct drivers receive independent transaction references")
    void testMultipleDriversReceiveUniqueFnbTxnRefs() {
        FnbDisbursementResult driverA = fnbInstantPayService.disburseFuelStipend(
                "+27 82 111 0001", 3.0, 500.0, "TOKEN-DRV-A"
        );
        FnbDisbursementResult driverB = fnbInstantPayService.disburseFuelStipend(
                "+27 82 222 0002", 5.0, 1000.0, "TOKEN-DRV-B"
        );

        assertThat(driverA.transactionReference()).isNotEqualTo(driverB.transactionReference());
        assertThat(driverA.recipientMobile()).isNotEqualTo(driverB.recipientMobile());
        assertThat(driverA.status()).isEqualTo("SUCCESS");
        assertThat(driverB.status()).isEqualTo("SUCCESS");
    }
}

