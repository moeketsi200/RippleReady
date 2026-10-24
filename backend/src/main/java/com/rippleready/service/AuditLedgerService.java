package com.rippleready.service;

import com.rippleready.model.AuditLedgerEntry;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class AuditLedgerService {

    private final List<AuditLedgerEntry> ledger = new CopyOnWriteArrayList<>();
    private String previousHash = "0000000000000000000000000000000000000000000000000000000000000000";

    public synchronized AuditLedgerEntry recordEvent(
            String eventType,
            String facilityId,
            String facilityName,
            String resourceId,
            String verificationToken,
            double litersDelivered,
            int beneficiariesProtected,
            String fnbTxnRef,
            String status
    ) {
        String entryId = "audit-" + UUID.randomUUID().toString().substring(0, 8);
        Instant now = Instant.now();

        String payload = previousHash + "|" + entryId + "|" + eventType + "|" + verificationToken + "|" + litersDelivered + "|" + now;
        String currentHash = computeSha256(payload);
        this.previousHash = currentHash;

        AuditLedgerEntry entry = new AuditLedgerEntry(
                entryId,
                now,
                eventType,
                facilityId,
                facilityName,
                resourceId,
                verificationToken,
                litersDelivered,
                beneficiariesProtected,
                fnbTxnRef,
                status,
                currentHash
        );

        ledger.add(entry);
        return entry;
    }

    public List<AuditLedgerEntry> getAuditTrail() {
        return new ArrayList<>(ledger);
    }

    private String computeSha256(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            return Integer.toHexString(data.hashCode());
        }
    }
}

