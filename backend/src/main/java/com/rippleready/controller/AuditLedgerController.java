package com.rippleready.controller;

import com.rippleready.model.AuditLedgerEntry;
import com.rippleready.service.AuditLedgerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
@CrossOrigin(origins = "*")
@Tag(name = "Tamper-Evident Audit Ledger", description = "Cryptographically hashed audit ledger for disaster response actions and disbursements")
public class AuditLedgerController {

    private final AuditLedgerService auditLedgerService;

    public AuditLedgerController(AuditLedgerService auditLedgerService) {
        this.auditLedgerService = auditLedgerService;
    }

    @GetMapping("/ledger")
    @Operation(summary = "Get complete tamper-evident audit ledger entries")
    public ResponseEntity<List<AuditLedgerEntry>> getLedger() {
        return ResponseEntity.ok(auditLedgerService.getAuditTrail());
    }
}

