package com.rippleready.controller;

import com.rippleready.model.MatchedResource;
import com.rippleready.model.MutualAidResource;
import com.rippleready.model.PledgeRecord;
import com.rippleready.model.PledgeRequest;
import com.rippleready.service.DataStore;
import com.rippleready.service.PledgeService;
import com.rippleready.service.RippleEngineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/resources")
@CrossOrigin(origins = "*")
@Tag(name = "Mutual Aid Resources", description = "Management, matching, and allocation of community capacity (boreholes, JoJo tanks)")
public class ResourceController {

    private final DataStore dataStore;
    private final PledgeService pledgeService;
    private final RippleEngineService rippleEngine;

    public ResourceController(
            DataStore dataStore,
            PledgeService pledgeService,
            RippleEngineService rippleEngine
    ) {
        this.dataStore = dataStore;
        this.pledgeService = pledgeService;
        this.rippleEngine = rippleEngine;
    }

    @GetMapping
    @Operation(summary = "List all registered mutual aid reserves")
    public ResponseEntity<List<MutualAidResource>> listResources() {
        return ResponseEntity.ok(dataStore.getAllResources());
    }

    @GetMapping("/match")
    @Operation(summary = "Find matching borehole and JoJo capacity within radius (Epic 3 / US-3.1)",
               description = "Returns nearby available capacity ranked ascending by distance and ensures volume is available.")
    public ResponseEntity<List<MatchedResource>> findMatchingResources(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(defaultValue = "3.0") double radiusKm,
            @RequestParam(defaultValue = "500.0") double requiredVolume
    ) {
        return ResponseEntity.ok(rippleEngine.findMatchingResources(lat, lon, radiusKm, requiredVolume));
    }

    @PostMapping
    @Operation(summary = "Register a new community mutual aid reserve")
    public ResponseEntity<MutualAidResource> registerResource(@RequestBody MutualAidResource resource) {
        String id = (resource.id() != null && !resource.id().isBlank())
                ? resource.id()
                : "res-" + UUID.randomUUID().toString().substring(0, 8);

        MutualAidResource created = new MutualAidResource(
                id,
                resource.providerName(),
                resource.resourceType(),
                resource.latitude(),
                resource.longitude(),
                resource.address(),
                resource.contactPerson(),
                resource.contactPhone(),
                resource.totalCapacity(),
                resource.availableCapacity(),
                resource.unitMeasure() != null ? resource.unitMeasure() : "Litres",
                true,
                resource.requiresTransport()
        );

        dataStore.addResource(created);
        return ResponseEntity.ok(created);
    }

    @PostMapping("/pledge")
    @Operation(summary = "Pledge and lock capacity for a threatened facility (Epic 3 / US-3.2)",
               description = "Decrements provider available capacity, generates OTP verification token, and marks facility as DISPATCHED.")
    public ResponseEntity<PledgeRecord> pledgeAid(@Valid @RequestBody PledgeRequest request) {
        return ResponseEntity.ok(pledgeService.createPledge(request));
    }

    @GetMapping("/pledges")
    @Operation(summary = "List all active and completed mutual aid pledges")
    public ResponseEntity<List<PledgeRecord>> listPledges() {
        return ResponseEntity.ok(dataStore.getAllPledges());
    }
}
