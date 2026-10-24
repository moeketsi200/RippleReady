package com.rippleready.controller;

import com.rippleready.model.*;
import com.rippleready.service.RippleEngineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ripple")
@CrossOrigin(origins = "*")
@Tag(name = "Ripple Engine", description = "Deterministic cascading interdependency calculation and mutual-aid matching")
public class RippleSimulationController {

    private final RippleEngineService rippleEngine;

    public RippleSimulationController(RippleEngineService rippleEngine) {
        this.rippleEngine = rippleEngine;
    }

    @PostMapping("/simulate")
    @Operation(summary = "Simulate outage cascade impact",
               description = "Calculates secondary downstream facility risks and routes closest mutual aid resources based on Haversine distance.")
    public ResponseEntity<RippleSimulationResponse> simulateCascade(
            @Valid @RequestBody RippleSimulationRequest request
    ) {
        return ResponseEntity.ok(rippleEngine.evaluateImpact(request));
    }

    @GetMapping("/demo-scenario")
    @Operation(summary = "Get pre-configured George (Ward 18) water pipe burst scenario",
               description = "Provides a ready-to-run outage request payload for live hackathon demonstration.")
    public ResponseEntity<RippleSimulationRequest> getDemoScenario() {
        RippleSimulationRequest demo = new RippleSimulationRequest(
                OutageCategory.WATER,
                OutageSeverity.CRITICAL_MAIN_RUPTURE,
                -33.9620, // York Street / Rosemoor junction in George
                22.4620,
                "Ward 18",
                "York Street primary feeder burst outside Pacaltsdorp bridge",
                6.0,   // 6-hour expected repair window
                2.5,   // 2.5 km impact radius
                4.0    // 4.0 km sourcing radius for mutual aid
        );
        return ResponseEntity.ok(demo);
    }
}

