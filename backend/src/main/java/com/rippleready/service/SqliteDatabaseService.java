package com.rippleready.service;

import com.rippleready.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.sql.*;
import java.time.Instant;
import java.util.*;

@Service
public class SqliteDatabaseService implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(SqliteDatabaseService.class);

    private final String dbPath;
    private final String jdbcUrl;
    private final Object lock = new Object();
    private Connection connection;

    public SqliteDatabaseService() {
        this("data/rippleready.db");
    }

    public SqliteDatabaseService(@Value("${rippleready.database.sqlite-path:data/rippleready.db}") String dbPath) {
        this.dbPath = dbPath;
        if (dbPath.startsWith("jdbc:sqlite:")) {
            this.jdbcUrl = dbPath;
        } else if (":memory:".equals(dbPath)) {
            this.jdbcUrl = "jdbc:sqlite::memory:";
        } else {
            File file = new File(dbPath);
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            this.jdbcUrl = "jdbc:sqlite:" + dbPath;
        }

        initConnection();
        initSchema();
    }

    private void initConnection() {
        synchronized (lock) {
            try {
                this.connection = DriverManager.getConnection(jdbcUrl);
                try (Statement stmt = this.connection.createStatement()) {
                    if (!jdbcUrl.contains(":memory:")) {
                        stmt.execute("PRAGMA journal_mode = WAL;");
                        stmt.execute("PRAGMA synchronous = NORMAL;");
                    }
                    stmt.execute("PRAGMA busy_timeout = 5000;");
                    stmt.execute("PRAGMA foreign_keys = ON;");
                }
                log.info("SQLite connected successfully at: {}", jdbcUrl);
            } catch (SQLException e) {
                log.error("Failed to initialize SQLite connection to {}", jdbcUrl, e);
                throw new RuntimeException("SQLite initialization failed", e);
            }
        }
    }

    private Connection getConnection() throws SQLException {
        synchronized (lock) {
            if (connection == null || connection.isClosed()) {
                initConnection();
            }
            return connection;
        }
    }

    private void initSchema() {
        synchronized (lock) {
            try {
                Connection conn = getConnection();
                try (Statement stmt = conn.createStatement()) {
                    // 1. Facilities
                    stmt.execute("""
                        CREATE TABLE IF NOT EXISTS facilities (
                            id TEXT PRIMARY KEY,
                            name TEXT NOT NULL,
                            type TEXT NOT NULL,
                            latitude REAL NOT NULL,
                            longitude REAL NOT NULL,
                            address TEXT,
                            contact_person TEXT,
                            contact_phone TEXT,
                            beneficiary_count INTEGER NOT NULL,
                            buffer_hours INTEGER NOT NULL,
                            primary_need TEXT,
                            estimated_daily_loss_zar REAL NOT NULL,
                            verified INTEGER NOT NULL,
                            status TEXT NOT NULL
                        );
                    """);

                    // 2. Resources
                    stmt.execute("""
                        CREATE TABLE IF NOT EXISTS resources (
                            id TEXT PRIMARY KEY,
                            provider_name TEXT NOT NULL,
                            resource_type TEXT NOT NULL,
                            latitude REAL NOT NULL,
                            longitude REAL NOT NULL,
                            address TEXT,
                            contact_person TEXT,
                            contact_phone TEXT,
                            total_capacity REAL NOT NULL,
                            available_capacity REAL NOT NULL,
                            unit_measure TEXT NOT NULL,
                            verified INTEGER NOT NULL,
                            requires_transport INTEGER NOT NULL
                        );
                    """);

                    // 3. Incidents
                    stmt.execute("""
                        CREATE TABLE IF NOT EXISTS incidents (
                            id TEXT PRIMARY KEY,
                            reporter_id TEXT,
                            category TEXT NOT NULL,
                            severity TEXT NOT NULL,
                            latitude REAL NOT NULL,
                            longitude REAL NOT NULL,
                            ward TEXT NOT NULL,
                            status TEXT NOT NULL,
                            impact_radius_km REAL NOT NULL,
                            confidence_score INTEGER NOT NULL,
                            confirmation_votes INTEGER NOT NULL,
                            reported_at TEXT NOT NULL,
                            is_duplicate INTEGER NOT NULL,
                            linked_incident_id TEXT
                        );
                    """);

                    // 4. Pledges
                    stmt.execute("""
                        CREATE TABLE IF NOT EXISTS pledges (
                            id TEXT PRIMARY KEY,
                            resource_id TEXT NOT NULL,
                            resource_provider_name TEXT NOT NULL,
                            facility_id TEXT NOT NULL,
                            facility_name TEXT NOT NULL,
                            allocated_units REAL NOT NULL,
                            unit_measure TEXT NOT NULL,
                            pledger_contact TEXT,
                            notes TEXT,
                            status TEXT NOT NULL,
                            verification_token TEXT NOT NULL UNIQUE,
                            pledged_at TEXT NOT NULL,
                            verified_at TEXT,
                            driver_mobile TEXT,
                            fnb_payment_ref TEXT
                        );
                    """);

                    // 5. Audit Ledger
                    stmt.execute("""
                        CREATE TABLE IF NOT EXISTS audit_ledger (
                            id TEXT PRIMARY KEY,
                            timestamp TEXT NOT NULL,
                            event_type TEXT NOT NULL,
                            facility_id TEXT,
                            facility_name TEXT,
                            resource_id TEXT,
                            verification_token TEXT,
                            liters_delivered REAL NOT NULL,
                            beneficiaries_protected INTEGER NOT NULL,
                            fnb_txn_ref TEXT,
                            status TEXT NOT NULL,
                            audit_hash TEXT NOT NULL
                        );
                    """);

                    // Indexes for performance
                    stmt.execute("CREATE INDEX IF NOT EXISTS idx_pledges_token ON pledges(verification_token);");
                    stmt.execute("CREATE INDEX IF NOT EXISTS idx_facilities_status ON facilities(status);");
                    stmt.execute("CREATE INDEX IF NOT EXISTS idx_resources_type ON resources(resource_type);");
                }
            } catch (SQLException e) {
                log.error("Failed to create SQLite database schema", e);
                throw new RuntimeException("SQLite schema creation failed", e);
            }
        }
    }

    // ================= FACILITIES =================

    public void saveFacility(DependentFacility f) {
        synchronized (lock) {
            String sql = """
                INSERT OR REPLACE INTO facilities (
                    id, name, type, latitude, longitude, address,
                    contact_person, contact_phone, beneficiary_count,
                    buffer_hours, primary_need, estimated_daily_loss_zar,
                    verified, status
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
            """;
            try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
                ps.setString(1, f.id());
                ps.setString(2, f.name());
                ps.setString(3, f.type().name());
                ps.setDouble(4, f.latitude());
                ps.setDouble(5, f.longitude());
                ps.setString(6, f.address());
                ps.setString(7, f.contactPerson());
                ps.setString(8, f.contactPhone());
                ps.setInt(9, f.beneficiaryCount());
                ps.setInt(10, f.bufferHours());
                ps.setString(11, f.primaryNeed());
                ps.setDouble(12, f.estimatedDailyLossZar());
                ps.setInt(13, f.verified() ? 1 : 0);
                ps.setString(14, f.status());
                ps.executeUpdate();
            } catch (SQLException e) {
                log.error("Error saving facility: {}", f.id(), e);
                throw new RuntimeException("Error saving facility to SQLite", e);
            }
        }
    }

    public List<DependentFacility> getAllFacilities() {
        synchronized (lock) {
            List<DependentFacility> list = new ArrayList<>();
            String sql = "SELECT * FROM facilities ORDER BY id;";
            try (PreparedStatement ps = getConnection().prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapFacility(rs));
                }
            } catch (SQLException e) {
                log.error("Error reading facilities from SQLite", e);
            }
            return list;
        }
    }

    public Optional<DependentFacility> getFacilityById(String id) {
        synchronized (lock) {
            String sql = "SELECT * FROM facilities WHERE id = ?;";
            try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
                ps.setString(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapFacility(rs));
                    }
                }
            } catch (SQLException e) {
                log.error("Error reading facility: {}", id, e);
            }
            return Optional.empty();
        }
    }

    private DependentFacility mapFacility(ResultSet rs) throws SQLException {
        return new DependentFacility(
                rs.getString("id"),
                rs.getString("name"),
                FacilityType.valueOf(rs.getString("type")),
                rs.getDouble("latitude"),
                rs.getDouble("longitude"),
                rs.getString("address"),
                rs.getString("contact_person"),
                rs.getString("contact_phone"),
                rs.getInt("beneficiary_count"),
                rs.getInt("buffer_hours"),
                rs.getString("primary_need"),
                rs.getDouble("estimated_daily_loss_zar"),
                rs.getInt("verified") == 1,
                rs.getString("status")
        );
    }

    // ================= RESOURCES =================

    public void saveResource(MutualAidResource r) {
        synchronized (lock) {
            String sql = """
                INSERT OR REPLACE INTO resources (
                    id, provider_name, resource_type, latitude, longitude,
                    address, contact_person, contact_phone, total_capacity,
                    available_capacity, unit_measure, verified, requires_transport
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
            """;
            try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
                ps.setString(1, r.id());
                ps.setString(2, r.providerName());
                ps.setString(3, r.resourceType().name());
                ps.setDouble(4, r.latitude());
                ps.setDouble(5, r.longitude());
                ps.setString(6, r.address());
                ps.setString(7, r.contactPerson());
                ps.setString(8, r.contactPhone());
                ps.setDouble(9, r.totalCapacity());
                ps.setDouble(10, r.availableCapacity());
                ps.setString(11, r.unitMeasure());
                ps.setInt(12, r.verified() ? 1 : 0);
                ps.setInt(13, r.requiresTransport() ? 1 : 0);
                ps.executeUpdate();
            } catch (SQLException e) {
                log.error("Error saving resource: {}", r.id(), e);
                throw new RuntimeException("Error saving resource to SQLite", e);
            }
        }
    }

    public List<MutualAidResource> getAllResources() {
        synchronized (lock) {
            List<MutualAidResource> list = new ArrayList<>();
            String sql = "SELECT * FROM resources ORDER BY id;";
            try (PreparedStatement ps = getConnection().prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResource(rs));
                }
            } catch (SQLException e) {
                log.error("Error reading resources from SQLite", e);
            }
            return list;
        }
    }

    public Optional<MutualAidResource> getResourceById(String id) {
        synchronized (lock) {
            String sql = "SELECT * FROM resources WHERE id = ?;";
            try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
                ps.setString(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResource(rs));
                    }
                }
            } catch (SQLException e) {
                log.error("Error reading resource: {}", id, e);
            }
            return Optional.empty();
        }
    }

    private MutualAidResource mapResource(ResultSet rs) throws SQLException {
        return new MutualAidResource(
                rs.getString("id"),
                rs.getString("provider_name"),
                ResourceType.valueOf(rs.getString("resource_type")),
                rs.getDouble("latitude"),
                rs.getDouble("longitude"),
                rs.getString("address"),
                rs.getString("contact_person"),
                rs.getString("contact_phone"),
                rs.getDouble("total_capacity"),
                rs.getDouble("available_capacity"),
                rs.getString("unit_measure"),
                rs.getInt("verified") == 1,
                rs.getInt("requires_transport") == 1
        );
    }

    // ================= INCIDENTS =================

    public void saveIncident(IncidentRecord inc) {
        synchronized (lock) {
            String sql = """
                INSERT OR REPLACE INTO incidents (
                    id, reporter_id, category, severity, latitude, longitude,
                    ward, status, impact_radius_km, confidence_score,
                    confirmation_votes, reported_at, is_duplicate, linked_incident_id
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
            """;
            try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
                ps.setString(1, inc.id());
                ps.setString(2, inc.reporterId());
                ps.setString(3, inc.category());
                ps.setString(4, inc.severity());
                ps.setDouble(5, inc.latitude());
                ps.setDouble(6, inc.longitude());
                ps.setString(7, inc.ward());
                ps.setString(8, inc.status());
                ps.setDouble(9, inc.impactRadiusKm());
                ps.setInt(10, inc.confidenceScore());
                ps.setInt(11, inc.confirmationVotes());
                ps.setString(12, inc.reportedAt().toString());
                ps.setInt(13, inc.isDuplicate() ? 1 : 0);
                ps.setString(14, inc.linkedIncidentId());
                ps.executeUpdate();
            } catch (SQLException e) {
                log.error("Error saving incident: {}", inc.id(), e);
                throw new RuntimeException("Error saving incident to SQLite", e);
            }
        }
    }

    public List<IncidentRecord> getAllIncidents() {
        synchronized (lock) {
            List<IncidentRecord> list = new ArrayList<>();
            String sql = "SELECT * FROM incidents ORDER BY reported_at ASC;";
            try (PreparedStatement ps = getConnection().prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapIncident(rs));
                }
            } catch (SQLException e) {
                log.error("Error reading incidents from SQLite", e);
            }
            return list;
        }
    }

    public Optional<IncidentRecord> getIncidentById(String id) {
        synchronized (lock) {
            String sql = "SELECT * FROM incidents WHERE id = ?;";
            try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
                ps.setString(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapIncident(rs));
                    }
                }
            } catch (SQLException e) {
                log.error("Error reading incident: {}", id, e);
            }
            return Optional.empty();
        }
    }

    private IncidentRecord mapIncident(ResultSet rs) throws SQLException {
        return new IncidentRecord(
                rs.getString("id"),
                rs.getString("reporter_id"),
                rs.getString("category"),
                rs.getString("severity"),
                rs.getDouble("latitude"),
                rs.getDouble("longitude"),
                rs.getString("ward"),
                rs.getString("status"),
                rs.getDouble("impact_radius_km"),
                rs.getInt("confidence_score"),
                rs.getInt("confirmation_votes"),
                Instant.parse(rs.getString("reported_at")),
                rs.getInt("is_duplicate") == 1,
                rs.getString("linked_incident_id")
        );
    }

    // ================= PLEDGES =================

    public void savePledge(PledgeRecord p) {
        synchronized (lock) {
            String sql = """
                INSERT OR REPLACE INTO pledges (
                    id, resource_id, resource_provider_name, facility_id, facility_name,
                    allocated_units, unit_measure, pledger_contact, notes,
                    status, verification_token, pledged_at, verified_at,
                    driver_mobile, fnb_payment_ref
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
            """;
            try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
                ps.setString(1, p.id());
                ps.setString(2, p.resourceId());
                ps.setString(3, p.resourceProviderName());
                ps.setString(4, p.facilityId());
                ps.setString(5, p.facilityName());
                ps.setDouble(6, p.allocatedUnits());
                ps.setString(7, p.unitMeasure());
                ps.setString(8, p.pledgerContact());
                ps.setString(9, p.notes());
                ps.setString(10, p.status());
                ps.setString(11, p.verificationToken());
                ps.setString(12, p.pledgedAt().toString());
                ps.setString(13, p.verifiedAt() != null ? p.verifiedAt().toString() : null);
                ps.setString(14, p.driverMobile());
                ps.setString(15, p.fnbPaymentRef());
                ps.executeUpdate();
            } catch (SQLException e) {
                log.error("Error saving pledge: {}", p.id(), e);
                throw new RuntimeException("Error saving pledge to SQLite", e);
            }
        }
    }

    public List<PledgeRecord> getAllPledges() {
        synchronized (lock) {
            List<PledgeRecord> list = new ArrayList<>();
            String sql = "SELECT * FROM pledges ORDER BY pledged_at ASC;";
            try (PreparedStatement ps = getConnection().prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapPledge(rs));
                }
            } catch (SQLException e) {
                log.error("Error reading pledges from SQLite", e);
            }
            return list;
        }
    }

    public Optional<PledgeRecord> getPledgeById(String id) {
        synchronized (lock) {
            String sql = "SELECT * FROM pledges WHERE id = ?;";
            try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
                ps.setString(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapPledge(rs));
                    }
                }
            } catch (SQLException e) {
                log.error("Error reading pledge: {}", id, e);
            }
            return Optional.empty();
        }
    }

    public Optional<PledgeRecord> getPledgeByVerificationToken(String token) {
        synchronized (lock) {
            String sql = "SELECT * FROM pledges WHERE LOWER(verification_token) = LOWER(?);";
            try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
                ps.setString(1, token.trim());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapPledge(rs));
                    }
                }
            } catch (SQLException e) {
                log.error("Error reading pledge by token: {}", token, e);
            }
            return Optional.empty();
        }
    }

    private PledgeRecord mapPledge(ResultSet rs) throws SQLException {
        String verifiedAtStr = rs.getString("verified_at");
        Instant verifiedAt = (verifiedAtStr != null && !verifiedAtStr.isBlank())
                ? Instant.parse(verifiedAtStr)
                : null;

        return new PledgeRecord(
                rs.getString("id"),
                rs.getString("resource_id"),
                rs.getString("resource_provider_name"),
                rs.getString("facility_id"),
                rs.getString("facility_name"),
                rs.getDouble("allocated_units"),
                rs.getString("unit_measure"),
                rs.getString("pledger_contact"),
                rs.getString("notes"),
                rs.getString("status"),
                rs.getString("verification_token"),
                Instant.parse(rs.getString("pledged_at")),
                verifiedAt,
                rs.getString("driver_mobile"),
                rs.getString("fnb_payment_ref")
        );
    }

    // ================= AUDIT LEDGER =================

    public void saveAuditEntry(AuditLedgerEntry a) {
        synchronized (lock) {
            String sql = """
                INSERT OR REPLACE INTO audit_ledger (
                    id, timestamp, event_type, facility_id, facility_name,
                    resource_id, verification_token, liters_delivered,
                    beneficiaries_protected, fnb_txn_ref, status, audit_hash
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
            """;
            try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
                ps.setString(1, a.id());
                ps.setString(2, a.timestamp().toString());
                ps.setString(3, a.eventType());
                ps.setString(4, a.facilityId());
                ps.setString(5, a.facilityName());
                ps.setString(6, a.resourceId());
                ps.setString(7, a.verificationToken());
                ps.setDouble(8, a.litersDelivered());
                ps.setInt(9, a.beneficiariesProtected());
                ps.setString(10, a.fnbTxnRef());
                ps.setString(11, a.status());
                ps.setString(12, a.auditHash());
                ps.executeUpdate();
            } catch (SQLException e) {
                log.error("Error saving audit ledger entry: {}", a.id(), e);
                throw new RuntimeException("Error saving audit ledger entry to SQLite", e);
            }
        }
    }

    public List<AuditLedgerEntry> getAllAuditEntries() {
        synchronized (lock) {
            List<AuditLedgerEntry> list = new ArrayList<>();
            String sql = "SELECT * FROM audit_ledger ORDER BY timestamp ASC;";
            try (PreparedStatement ps = getConnection().prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapAuditEntry(rs));
                }
            } catch (SQLException e) {
                log.error("Error reading audit ledger from SQLite", e);
            }
            return list;
        }
    }

    public Optional<String> getLastAuditHash() {
        synchronized (lock) {
            String sql = "SELECT audit_hash FROM audit_ledger ORDER BY timestamp DESC LIMIT 1;";
            try (PreparedStatement ps = getConnection().prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.ofNullable(rs.getString("audit_hash"));
                }
            } catch (SQLException e) {
                log.error("Error reading last audit hash from SQLite", e);
            }
            return Optional.empty();
        }
    }

    private AuditLedgerEntry mapAuditEntry(ResultSet rs) throws SQLException {
        return new AuditLedgerEntry(
                rs.getString("id"),
                Instant.parse(rs.getString("timestamp")),
                rs.getString("event_type"),
                rs.getString("facility_id"),
                rs.getString("facility_name"),
                rs.getString("resource_id"),
                rs.getString("verification_token"),
                rs.getDouble("liters_delivered"),
                rs.getInt("beneficiaries_protected"),
                rs.getString("fnb_txn_ref"),
                rs.getString("status"),
                rs.getString("audit_hash")
        );
    }

    // ================= MANAGEMENT & SEEDING =================

    public void clearAllTables() {
        synchronized (lock) {
            try {
                Connection conn = getConnection();
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("DELETE FROM pledges;");
                    stmt.execute("DELETE FROM incidents;");
                    stmt.execute("DELETE FROM facilities;");
                    stmt.execute("DELETE FROM resources;");
                    stmt.execute("DELETE FROM audit_ledger;");
                }
            } catch (SQLException e) {
                log.error("Failed to clear SQLite tables", e);
            }
        }
    }

    public void seedInitialData() {
        synchronized (lock) {
            // Seed Facilities
            saveFacility(new DependentFacility(
                    "fac-1", "Little Stars Crèche", FacilityType.CRECHE,
                    -33.9632, 22.4612, "42 Beer St, Rosemoor, George",
                    "Sis' Thandiwe", "+27 82 555 4101", 40, 2,
                    "Drinking water for 40 toddlers & sanitation", 3200.0, true, "THREATENED"
            ));

            saveFacility(new DependentFacility(
                    "fac-2", "Mama K's Spaza & Corner Eatery", FacilityType.FOOD_MERCHANT,
                    -33.9615, 22.4640, "18 Woltemade St, Rosemoor, George",
                    "Mama Khanyisa", "+27 73 555 7720", 120, 3,
                    "Sanitation & food preparation water", 4800.0, true, "STABLE"
            ));

            saveFacility(new DependentFacility(
                    "fac-3", "Rosemoor Fragile & Elderly Care", FacilityType.ELDERLY_CARE,
                    -33.9590, 22.4580, "5 Attaqua Way, Rosemoor, George",
                    "Nurse Matron Jacobs", "+27 84 555 3391", 28, 1,
                    "Medical sanitation & sterile drinking water", 8500.0, true, "STABLE"
            ));

            saveFacility(new DependentFacility(
                    "fac-4", "St. Mark's Community Soup Kitchen", FacilityType.SOUP_KITCHEN,
                    -33.9650, 22.4600, "Cathedral Lane, George Central",
                    "Father Joseph", "+27 82 555 9012", 200, 2,
                    "250L cooking water for midday meal distribution", 2500.0, true, "STABLE"
            ));

            saveFacility(new DependentFacility(
                    "fac-5", "York Street Commercial Laundromat & SME Hub", FacilityType.LOCAL_SME,
                    -33.9605, 22.4625, "104 York St, George Central",
                    "Dawie van der Merwe", "+27 83 555 1198", 15, 4,
                    "Utility washing water to keep Speedpoint trade active", 6200.0, true, "STABLE"
            ));

            saveFacility(new DependentFacility(
                    "fac-hosp-central", "George Central Hospital", FacilityType.CLINIC,
                    -33.9900, 22.5000, "Davidson Rd, George",
                    "Dr. Petersen", "+27 44 555 2000", 350, 12,
                    "Secondary emergency backup connection", 25000.0, true, "STABLE"
            ));

            // Seed Resources
            saveResource(new MutualAidResource(
                    "res-1", "Hendricks Hardware & Agri Supplies (Borehole)",
                    ResourceType.BOREHOLE_NON_POTABLE, -33.9580, 22.4650,
                    "77 Hibernia St, George", "Oom Pieter Hendricks", "+27 82 555 0192",
                    1500.0, 1500.0, "Litres", true, true
            ));

            saveResource(new MutualAidResource(
                    "res-2", "St. Mark's Church Reservoir (Treated JoJo Tank)",
                    ResourceType.JOJO_POTABLE, -33.9640, 22.4590,
                    "St Mark St, George Central", "Deacon Andrew", "+27 83 555 0143",
                    5000.0, 5000.0, "Litres", true, false
            ));

            saveResource(new MutualAidResource(
                    "res-3", "Garden Route Volunteer Bowser Unit #1",
                    ResourceType.MOBILE_BOWSER, -33.9610, 22.4550,
                    "Depot 4, Industrial Area, George", "Sipho Mthembu", "+27 79 555 0122",
                    2000.0, 2000.0, "Litres", true, false
            ));
        }
    }

    public Map<String, Object> getDatabaseStats() {
        synchronized (lock) {
            Map<String, Object> stats = new LinkedHashMap<>();
            stats.put("status", "HEALTHY");
            stats.put("engine", "SQLite 3");
            stats.put("jdbcUrl", jdbcUrl);
            stats.put("isInMemory", jdbcUrl.contains(":memory:"));

            if (!jdbcUrl.contains(":memory:")) {
                File file = new File(dbPath.replace("jdbc:sqlite:", ""));
                stats.put("fileSizeBytes", file.exists() ? file.length() : 0);
                stats.put("filePath", file.getAbsolutePath());
            }

            try {
                Connection conn = getConnection();
                try (Statement stmt = conn.createStatement()) {
                    try (ResultSet rs = stmt.executeQuery("PRAGMA journal_mode;")) {
                        if (rs.next()) stats.put("journalMode", rs.getString(1));
                    }
                    try (ResultSet rs = stmt.executeQuery("SELECT count(*) FROM facilities;")) {
                        if (rs.next()) stats.put("facilityCount", rs.getInt(1));
                    }
                    try (ResultSet rs = stmt.executeQuery("SELECT count(*) FROM resources;")) {
                        if (rs.next()) stats.put("resourceCount", rs.getInt(1));
                    }
                    try (ResultSet rs = stmt.executeQuery("SELECT count(*) FROM incidents;")) {
                        if (rs.next()) stats.put("incidentCount", rs.getInt(1));
                    }
                    try (ResultSet rs = stmt.executeQuery("SELECT count(*) FROM pledges;")) {
                        if (rs.next()) stats.put("pledgeCount", rs.getInt(1));
                    }
                    try (ResultSet rs = stmt.executeQuery("SELECT count(*) FROM audit_ledger;")) {
                        if (rs.next()) stats.put("auditLedgerCount", rs.getInt(1));
                    }
                }
            } catch (SQLException e) {
                stats.put("error", e.getMessage());
            }

            return stats;
        }
    }

    @Override
    public void close() {
        synchronized (lock) {
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                log.error("Error closing SQLite connection", e);
            }
        }
    }
}
