package com.rippleready;

import com.rippleready.model.*;
import com.rippleready.service.AuditLedgerService;
import com.rippleready.service.SqliteDatabaseService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class SqlitePersistenceTest {

    private String testDbPath;
    private SqliteDatabaseService sqliteService;

    @BeforeEach
    void setUp() {
        testDbPath = "target/test-rippleready-" + System.nanoTime() + ".db";
        sqliteService = new SqliteDatabaseService(testDbPath);
    }

    @AfterEach
    void tearDown() {
        if (sqliteService != null) {
            sqliteService.close();
        }
        new File(testDbPath).delete();
        new File(testDbPath + "-wal").delete();
        new File(testDbPath + "-shm").delete();
    }

    @Test
    @DisplayName("SQLite: Tables are created with WAL journaling enabled and healthy status")
    void testSqliteSchemaAndWalMode() {
        Map<String, Object> stats = sqliteService.getDatabaseStats();
        assertThat(stats.get("status")).isEqualTo("HEALTHY");
        assertThat(stats.get("engine")).isEqualTo("SQLite 3");
        assertThat(stats.get("journalMode")).isEqualTo("wal");
    }

    @Test
    @DisplayName("SQLite: Default seeding populates 6 facilities and 3 mutual aid resources")
    void testInitialDataSeeding() {
        sqliteService.seedInitialData();

        List<DependentFacility> facilities = sqliteService.getAllFacilities();
        assertThat(facilities).hasSize(6);

        Optional<DependentFacility> creche = sqliteService.getFacilityById("fac-1");
        assertThat(creche).isPresent();
        assertThat(creche.get().name()).isEqualTo("Little Stars Crèche");
        assertThat(creche.get().beneficiaryCount()).isEqualTo(40);

        List<MutualAidResource> resources = sqliteService.getAllResources();
        assertThat(resources).hasSize(3);

        Optional<MutualAidResource> jojo = sqliteService.getResourceById("res-2");
        assertThat(jojo).isPresent();
        assertThat(jojo.get().providerName()).contains("St. Mark's Church");
        assertThat(jojo.get().totalCapacity()).isEqualTo(5000.0);
    }

    @Test
    @DisplayName("SQLite: Incidents are persisted and retrieved with exact precision")
    void testIncidentPersistence() {
        IncidentRecord incident = new IncidentRecord(
                "inc-sqlite-001",
                "rep-george-01",
                "WATER",
                "CRITICAL_MAIN_RUPTURE",
                -33.9620,
                22.4620,
                "Ward 18",
                "VERIFIED",
                2.0,
                90,
                4,
                Instant.now(),
                false,
                null
        );

        sqliteService.saveIncident(incident);

        Optional<IncidentRecord> retrieved = sqliteService.getIncidentById("inc-sqlite-001");
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().ward()).isEqualTo("Ward 18");
        assertThat(retrieved.get().category()).isEqualTo("WATER");
        assertThat(retrieved.get().impactRadiusKm()).isEqualTo(2.0);
        assertThat(retrieved.get().confidenceScore()).isEqualTo(90);
    }

    @Test
    @DisplayName("SQLite: Pledges can be queried quickly by unique verification OTP token")
    void testPledgePersistenceAndTokenLookup() {
        PledgeRecord pledge = new PledgeRecord(
                "pledge-sqlite-01",
                "res-2",
                "St. Mark's Church Reservoir",
                "fac-1",
                "Little Stars Crèche",
                500.0,
                "Litres",
                "+27 82 555 0192",
                "Priority daycare dispatch",
                "DISPATCHED",
                "TOKEN-SQLITE-99",
                Instant.now(),
                null,
                "+27 82 555 9901",
                null
        );

        sqliteService.savePledge(pledge);

        // Case-insensitive token lookup
        Optional<PledgeRecord> retrieved = sqliteService.getPledgeByVerificationToken("token-sqlite-99");
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().facilityName()).isEqualTo("Little Stars Crèche");
        assertThat(retrieved.get().allocatedUnits()).isEqualTo(500.0);
        assertThat(retrieved.get().status()).isEqualTo("DISPATCHED");
    }

    @Test
    @DisplayName("SQLite: Audit ledger records SHA-256 hashes and survives service re-instantiation")
    void testAuditLedgerPersistenceAcrossInstances() {
        AuditLedgerService ledgerService = new AuditLedgerService(sqliteService);

        AuditLedgerEntry entry1 = ledgerService.recordEvent(
                "OUTAGE_DETECTED", "fac-1", "Little Stars Crèche", null,
                null, 0.0, 40, null, "THREATENED"
        );

        AuditLedgerEntry entry2 = ledgerService.recordEvent(
                "DELIVERY_VERIFIED", "fac-1", "Little Stars Crèche", "res-2",
                "WTR-888", 500.0, 40, "FNB-TXN-1001", "RESOLVED"
        );

        assertThat(entry1.auditHash()).isNotEmpty();
        assertThat(entry2.auditHash()).isNotEmpty();
        assertThat(entry2.auditHash()).isNotEqualTo(entry1.auditHash());

        // Create a new AuditLedgerService instance pointing to the same SQLite database
        AuditLedgerService recoveredLedger = new AuditLedgerService(sqliteService);
        List<AuditLedgerEntry> entries = recoveredLedger.getAuditTrail();

        assertThat(entries).hasSize(2);
        assertThat(entries.get(0).id()).isEqualTo(entry1.id());
        assertThat(entries.get(1).id()).isEqualTo(entry2.id());
        assertThat(entries.get(1).auditHash()).isEqualTo(entry2.auditHash());

        // The next entry must chain from the previous hash
        AuditLedgerEntry entry3 = recoveredLedger.recordEvent(
                "PAYMENT_DISBURSED", "fac-1", "Little Stars Crèche", "res-2",
                "WTR-888", 500.0, 40, "FNB-PAY-2002", "PAID"
        );
        assertThat(entry3.auditHash()).isNotEmpty();
        assertThat(recoveredLedger.getAuditTrail()).hasSize(3);
    }

    @Test
    @DisplayName("SQLite: Data written by one connection is immediately readable by another on disk")
    void testDataSurvivesDatabaseReopen() {
        sqliteService.seedInitialData();
        sqliteService.saveFacility(new DependentFacility(
                "fac-test-extra",
                "Custom Community Center",
                FacilityType.CRECHE,
                -33.9600, 22.4600,
                "12 Market St", "Sipho", "+27 82 000 0000",
                50, 3, "Potable water", 1000.0, true, "STABLE"
        ));
        sqliteService.close();

        // Reopen new service pointing to same file
        SqliteDatabaseService reopened = new SqliteDatabaseService(testDbPath);
        try {
            Optional<DependentFacility> custom = reopened.getFacilityById("fac-test-extra");
            assertThat(custom).isPresent();
            assertThat(custom.get().name()).isEqualTo("Custom Community Center");
            assertThat(reopened.getAllFacilities()).hasSize(7);
        } finally {
            reopened.close();
        }
    }
}
