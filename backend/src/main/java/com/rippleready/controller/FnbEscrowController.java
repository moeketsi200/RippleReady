package com.rippleready.controller;

import com.rippleready.model.FnbDisbursementResult;
import com.rippleready.service.FnbInstantPayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/fnb")
@CrossOrigin(origins = "*")
@Tag(name = "FNB Banking Rails", description = "Instant Pay, eWallet micro-escrows, and SME merchant disaster protection")
public class FnbEscrowController {

    private final FnbInstantPayService fnbInstantPayService;

    public FnbEscrowController(FnbInstantPayService fnbInstantPayService) {
        this.fnbInstantPayService = fnbInstantPayService;
    }

    @PostMapping("/disburse-fuel-grant")
    @Operation(summary = "Manually trigger FNB Instant Pay fuel grant disbursement",
               description = "Dispatches a R150 micro-grant to volunteer driver's mobile number.")
    public ResponseEntity<FnbDisbursementResult> disburseFuelGrant(@RequestBody Map<String, Object> payload) {
        String mobile = (String) payload.getOrDefault("driverMobile", "+27 82 555 9901");
        double roundTrip = Double.parseDouble(payload.getOrDefault("roundTripKm", "4.0").toString());
        double volume = Double.parseDouble(payload.getOrDefault("litersDelivered", "1000.0").toString());
        String token = (String) payload.getOrDefault("verificationToken", "TOKEN-MANUAL");

        return ResponseEntity.ok(fnbInstantPayService.disburseFuelStipend(mobile, roundTrip, volume, token));
    }
}

