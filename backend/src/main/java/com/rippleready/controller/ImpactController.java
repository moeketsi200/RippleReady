package com.rippleready.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rippleready.model.ImpactSummaryResponse;
import com.rippleready.service.PledgeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/impact")
@CrossOrigin(origins = "*")
@Tag(name = "Impact Ledger", description = "Quantified economic and human disruption metrics for the live dashboard")
public class ImpactController {

    private final PledgeService pledgeService;

    public ImpactController(PledgeService pledgeService) {
        this.pledgeService = pledgeService;
    }

    @GetMapping("/summary")
    @Operation(summary = "Get aggregated impact metrics (hours averted, economic loss prevented, water dispatched)")
    public ResponseEntity<ImpactSummaryResponse> getSummary() {
        return ResponseEntity.ok(pledgeService.getImpactSummary());
    }
}

