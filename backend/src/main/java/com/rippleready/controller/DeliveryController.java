package com.rippleready.controller;

import com.rippleready.model.DeliveryVerificationRequest;
import com.rippleready.model.DeliveryVerificationResponse;
import com.rippleready.service.PledgeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/deliveries")
@CrossOrigin(origins = "*")
@Tag(name = "Delivery Verification & FNB Escrow", description = "Handshake verification token redemption, map resolution, and automated FNB Instant Pay payouts")
public class DeliveryController {

    private final PledgeService pledgeService;

    public DeliveryController(PledgeService pledgeService) {
        this.pledgeService = pledgeService;
    }

    @PostMapping("/verify")
    @Operation(summary = "Confirm delivery handshake using verification token (Epic 4 / US-4.1 & Epic 5 / US-5.1)",
               description = "Transitions facility status to RESOLVED (turns green), increments ledger, and triggers automated FNB Instant Pay fuel stipend.")
    public ResponseEntity<DeliveryVerificationResponse> verifyDelivery(
            @Valid @RequestBody DeliveryVerificationRequest request
    ) {
        return ResponseEntity.ok(pledgeService.verifyDelivery(request));
    }
}

