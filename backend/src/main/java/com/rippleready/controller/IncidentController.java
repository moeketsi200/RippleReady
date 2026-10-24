package com.rippleready.controller;

import com.rippleready.model.IncidentRecord;
import com.rippleready.model.IncidentReportRequest;
import com.rippleready.service.IncidentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/incidents")
@CrossOrigin(origins = "*")
@Tag(name = "Incident Ingestion", description = "Verified community anchor reporting, perimeters, and deduplication")
public class IncidentController {

    private final IncidentService incidentService;

    public IncidentController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @PostMapping
    @Operation(summary = "Report an active utility disruption (Epic 1)")
    public ResponseEntity<IncidentRecord> submitIncident(@RequestBody IncidentReportRequest request) {
        IncidentRecord incident = incidentService.reportIncident(request);
        if (incident.isDuplicate()) {
            return ResponseEntity.status(HttpStatus.OK).body(incident);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(incident);
    }

    @GetMapping
    @Operation(summary = "List all reported incidents")
    public ResponseEntity<List<IncidentRecord>> listIncidents() {
        return ResponseEntity.ok(incidentService.getAllIncidents());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get incident details by ID")
    public ResponseEntity<IncidentRecord> getIncident(@PathVariable String id) {
        return incidentService.getIncidentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

