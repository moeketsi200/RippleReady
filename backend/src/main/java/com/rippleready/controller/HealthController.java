package com.rippleready.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@CrossOrigin(origins = "*")
@Tag(name = "Health Check", description = "Liveness probe for cloud platform keep-alive")
public class HealthController {

    @GetMapping("/healthz")
    @Operation(summary = "Health status probe")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "RippleReady Backend",
                "version", "1.0.0",
                "track", "FNB App of the Year - Topic 6: Climate Resilience & Essential Services"
        ));
    }
}

