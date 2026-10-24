package com.rippleready.controller;

import com.rippleready.service.DataStore;
import com.rippleready.service.SqliteDatabaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/database")
@CrossOrigin(origins = "*")
@Tag(name = "Database", description = "Embedded SQLite database management, WAL journaling, and persistence metrics")
public class DatabaseController {

    private final SqliteDatabaseService sqliteService;
    private final DataStore dataStore;

    public DatabaseController(SqliteDatabaseService sqliteService, DataStore dataStore) {
        this.sqliteService = sqliteService;
        this.dataStore = dataStore;
    }

    @GetMapping("/status")
    @Operation(summary = "Get SQLite database status, table row counts, file size, and WAL mode")
    public ResponseEntity<Map<String, Object>> getDatabaseStatus() {
        return ResponseEntity.ok(sqliteService.getDatabaseStats());
    }

    @PostMapping("/reset")
    @Operation(summary = "Reset and re-seed the SQLite database with default George, Western Cape seed data")
    public ResponseEntity<Map<String, Object>> resetDatabase() {
        dataStore.reset();
        Map<String, Object> stats = sqliteService.getDatabaseStats();
        stats.put("message", "Database successfully reset and re-seeded with Western Cape community anchors.");
        return ResponseEntity.ok(stats);
    }
}
