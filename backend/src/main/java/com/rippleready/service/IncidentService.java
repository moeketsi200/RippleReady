package com.rippleready.service;

import com.rippleready.exception.InvalidCoordinatesException;
import com.rippleready.model.IncidentRecord;
import com.rippleready.model.IncidentReportRequest;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class IncidentService {

    private final DataStore dataStore;
    private final RippleEngineService rippleEngine;

    public IncidentService(DataStore dataStore, RippleEngineService rippleEngine) {
        this.dataStore = dataStore;
        this.rippleEngine = rippleEngine;
    }

    public synchronized IncidentRecord reportIncident(IncidentReportRequest request) {
        // US-1.2: Reject invalid or out-of-bounds geographic coordinates
        if (request.latitude() == null || request.longitude() == null
                || request.latitude() < -90.0 || request.latitude() > 90.0
                || request.longitude() < -180.0 || request.longitude() > 180.0) {
            throw new InvalidCoordinatesException("Coordinates out of permissible range");
        }

        // US-1.3: Deduplication check within 350 meters and 60 minutes
        Optional<IncidentRecord> existingMatch = dataStore.getAllIncidents().stream()
                .filter(inc -> inc.category().equalsIgnoreCase(request.category()))
                .filter(inc -> {
                    double dist = rippleEngine.calculateDistanceKm(
                            request.latitude(), request.longitude(),
                            inc.latitude(), inc.longitude()
                    );
                    long ageMinutes = Duration.between(inc.reportedAt(), Instant.now()).abs().toMinutes();
                    return dist <= 0.35 && ageMinutes <= 60;
                })
                .findFirst();

        if (existingMatch.isPresent()) {
            IncidentRecord original = existingMatch.get();
            IncidentRecord updated = original.withConfirmationVote();
            dataStore.updateIncident(updated);

            // Return duplicate representation pointing to original
            return new IncidentRecord(
                    original.id(),
                    request.reporterId(),
                    original.category(),
                    original.severity(),
                    request.latitude(),
                    request.longitude(),
                    original.ward(),
                    original.status(),
                    original.impactRadiusKm(),
                    updated.confidenceScore(),
                    updated.confirmationVotes(),
                    original.reportedAt(),
                    true, // isDuplicate = true
                    original.id()
            );
        }

        // US-1.1: Successful creation of verified incident
        String incidentId = dataStore.getNextIncidentId();
        double initialPerimeterKm = 2.0;

        IncidentRecord newIncident = new IncidentRecord(
                incidentId,
                request.reporterId(),
                request.category() != null ? request.category() : "Water",
                request.severity() != null ? request.severity() : "CRITICAL",
                request.latitude(),
                request.longitude(),
                request.ward() != null ? request.ward() : "Ward 18, George",
                "VERIFIED",
                initialPerimeterKm,
                100, // initial confidence score
                1,
                Instant.now(),
                false,
                null
        );

        dataStore.addIncident(newIncident);
        return newIncident;
    }

    public List<IncidentRecord> getAllIncidents() {
        return dataStore.getAllIncidents();
    }

    public Optional<IncidentRecord> getIncidentById(String id) {
        return dataStore.getIncidentById(id);
    }
}

