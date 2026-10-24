package com.rippleready.service;

import com.rippleready.model.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class PledgeService {

    private final DataStore dataStore;
    private final AuditLedgerService auditLedgerService;
    private final FnbInstantPayService fnbInstantPayService;

    public PledgeService(
            DataStore dataStore,
            AuditLedgerService auditLedgerService,
            FnbInstantPayService fnbInstantPayService
    ) {
        this.dataStore = dataStore;
        this.auditLedgerService = auditLedgerService;
        this.fnbInstantPayService = fnbInstantPayService;
    }

    /**
     * Epic 3 / US-3.2: Pledging and capacity reservation lock.
     * Decrements available capacity, generates verification token, and marks facility as DISPATCHED.
     */
    public synchronized PledgeRecord createPledge(PledgeRequest request) {
        MutualAidResource resource = dataStore.getResourceById(request.resourceId())
                .orElseThrow(() -> new IllegalArgumentException("Resource not found: " + request.resourceId()));

        DependentFacility facility = dataStore.getFacilityById(request.facilityId())
                .orElseThrow(() -> new IllegalArgumentException("Facility not found: " + request.facilityId()));

        if (resource.availableCapacity() < request.allocatedUnits()) {
            throw new IllegalStateException(String.format(
                    "Insufficient capacity: requested %.1f %s, but only %.1f %s available.",
                    request.allocatedUnits(), resource.unitMeasure(),
                    resource.availableCapacity(), resource.unitMeasure()
            ));
        }

        // 1. Decrement provider available capacity
        double newCapacity = resource.availableCapacity() - request.allocatedUnits();
        dataStore.updateResource(resource.withAvailableCapacity(newCapacity));

        // 2. Transition facility status to DISPATCHED
        dataStore.updateFacility(facility.withStatus("DISPATCHED"));

        // 3. Generate verification token (OTP / token)
        String token = (request.verificationToken() != null && !request.verificationToken().isBlank())
                ? request.verificationToken()
                : "TOKEN-" + (100 + (int)(Math.random() * 899));

        String driverMobile = (request.driverMobile() != null && !request.driverMobile().isBlank())
                ? request.driverMobile()
                : "+27 82 555 9901";

        PledgeRecord pledge = new PledgeRecord(
                UUID.randomUUID().toString(),
                resource.id(),
                resource.providerName(),
                facility.id(),
                facility.name(),
                request.allocatedUnits(),
                resource.unitMeasure(),
                request.pledgerContact(),
                request.notes(),
                "DISPATCHED",
                token,
                Instant.now(),
                null,
                driverMobile,
                null
        );

        dataStore.addPledge(pledge);

        auditLedgerService.recordEvent(
                "PLEDGE_RESERVATION_LOCKED",
                facility.id(),
                facility.name(),
                resource.id(),
                token,
                request.allocatedUnits(),
                facility.beneficiaryCount(),
                null,
                "DISPATCHED"
        );

        return pledge;
    }

    /**
     * Epic 4 & Epic 5 / US-4.1 & US-5.1: Handshake confirmation & FNB instant payment.
     * Closes loop, sets facility status to RESOLVED (turns green), and triggers FNB micro-escrow payout.
     */
    public synchronized DeliveryVerificationResponse verifyDelivery(DeliveryVerificationRequest request) {
        PledgeRecord pledge = dataStore.getPledgeByVerificationToken(request.verificationToken())
                .orElseThrow(() -> new IllegalArgumentException("Invalid or unknown verification token: " + request.verificationToken()));

        if ("RESOLVED".equalsIgnoreCase(pledge.status())) {
            throw new IllegalStateException("Delivery token has already been verified and closed.");
        }

        DependentFacility facility = dataStore.getFacilityById(pledge.facilityId())
                .orElseThrow(() -> new IllegalArgumentException("Facility not found: " + pledge.facilityId()));

        Instant now = Instant.now();

        // 1. Trigger automated FNB Instant Pay micro-grant (US-5.1)
        double roundTripKm = request.effectiveRoundTripKm();
        FnbDisbursementResult fnbDisbursement = fnbInstantPayService.disburseFuelStipend(
                pledge.driverMobile() != null ? pledge.driverMobile() : "+27 82 555 9901",
                roundTripKm,
                pledge.allocatedUnits(),
                pledge.verificationToken()
        );

        // 2. Transition facility status to RESOLVED (node turns green on map)
        dataStore.updateFacility(facility.withStatus("RESOLVED"));

        // 3. Transition pledge record to RESOLVED
        PledgeRecord resolvedPledge = pledge.withResolved(now, fnbDisbursement.transactionReference());
        dataStore.updatePledge(resolvedPledge);

        // 4. Record tamper-evident event on audit ledger
        auditLedgerService.recordEvent(
                "DELIVERY_HANDSHAKE_VERIFIED",
                facility.id(),
                facility.name(),
                pledge.resourceId(),
                pledge.verificationToken(),
                pledge.allocatedUnits(),
                facility.beneficiaryCount(),
                fnbDisbursement.transactionReference(),
                "RESOLVED"
        );

        return new DeliveryVerificationResponse(
                resolvedPledge.id(),
                facility.id(),
                facility.name(),
                "RESOLVED",
                "GREEN",
                pledge.allocatedUnits(),
                facility.beneficiaryCount(),
                fnbDisbursement,
                now
        );
    }

    public ImpactSummaryResponse getImpactSummary() {
        List<PledgeRecord> pledges = dataStore.getAllPledges();
        List<DependentFacility> facilities = dataStore.getAllFacilities();

        double totalWaterAllocated = pledges.stream()
                .mapToDouble(PledgeRecord::allocatedUnits)
                .sum();

        int totalProtected = facilities.stream()
                .filter(f -> "RESOLVED".equalsIgnoreCase(f.status()) || "DISPATCHED".equalsIgnoreCase(f.status()))
                .mapToInt(DependentFacility::beneficiaryCount)
                .sum();

        if (totalProtected == 0) {
            totalProtected = facilities.stream().mapToInt(DependentFacility::beneficiaryCount).sum();
        }

        double totalLossAverted = facilities.stream()
                .mapToDouble(DependentFacility::estimatedDailyLossZar)
                .sum();

        return new ImpactSummaryResponse(
                Math.max(1, dataStore.getAllIncidents().size()),
                facilities.size(),
                totalProtected,
                totalLossAverted,
                totalWaterAllocated,
                pledges.size(),
                18.4,
                "Ward 18, George, Western Cape"
        );
    }
}
