package com.rippleready.service;

import com.rippleready.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class DataStore {

    private final SqliteDatabaseService sqliteService;
    private final Map<String, DependentFacility> facilities = new ConcurrentHashMap<>();
    private final Map<String, MutualAidResource> resources = new ConcurrentHashMap<>();
    private final List<PledgeRecord> pledges = new CopyOnWriteArrayList<>();
    private final List<IncidentRecord> incidents = new CopyOnWriteArrayList<>();
    private final AtomicInteger incidentSequence = new AtomicInteger(1);

    public DataStore() {
        this(new SqliteDatabaseService());
    }

    @Autowired
    public DataStore(SqliteDatabaseService sqliteService) {
        this.sqliteService = sqliteService;
        initData();
    }

    private synchronized void initData() {
        facilities.clear();
        resources.clear();
        pledges.clear();
        incidents.clear();

        List<DependentFacility> savedFacilities = sqliteService.getAllFacilities();
        if (savedFacilities.isEmpty()) {
            sqliteService.seedInitialData();
            savedFacilities = sqliteService.getAllFacilities();
        }

        for (DependentFacility f : savedFacilities) {
            facilities.put(f.id(), f);
        }

        for (MutualAidResource r : sqliteService.getAllResources()) {
            resources.put(r.id(), r);
        }

        pledges.addAll(sqliteService.getAllPledges());

        List<IncidentRecord> savedIncidents = sqliteService.getAllIncidents();
        incidents.addAll(savedIncidents);
        int maxSeq = 1;
        for (IncidentRecord inc : savedIncidents) {
            if (inc.id() != null && inc.id().startsWith("inc-")) {
                try {
                    int seq = Integer.parseInt(inc.id().substring(4));
                    if (seq >= maxSeq) {
                        maxSeq = seq + 1;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        incidentSequence.set(maxSeq);
    }

    public synchronized void reset() {
        sqliteService.clearAllTables();
        sqliteService.seedInitialData();
        incidentSequence.set(1);
        initData();
    }

    public List<DependentFacility> getAllFacilities() {
        return new ArrayList<>(facilities.values());
    }

    public Optional<DependentFacility> getFacilityById(String id) {
        return Optional.ofNullable(facilities.get(id));
    }

    public void addFacility(DependentFacility facility) {
        facilities.put(facility.id(), facility);
        sqliteService.saveFacility(facility);
    }

    public void updateFacility(DependentFacility facility) {
        facilities.put(facility.id(), facility);
        sqliteService.saveFacility(facility);
    }

    public List<MutualAidResource> getAllResources() {
        return new ArrayList<>(resources.values());
    }

    public Optional<MutualAidResource> getResourceById(String id) {
        return Optional.ofNullable(resources.get(id));
    }

    public void addResource(MutualAidResource resource) {
        resources.put(resource.id(), resource);
        sqliteService.saveResource(resource);
    }

    public void updateResource(MutualAidResource resource) {
        resources.put(resource.id(), resource);
        sqliteService.saveResource(resource);
    }

    public List<PledgeRecord> getAllPledges() {
        return new ArrayList<>(pledges);
    }

    public Optional<PledgeRecord> getPledgeById(String id) {
        return pledges.stream().filter(p -> p.id().equals(id)).findFirst();
    }

    public Optional<PledgeRecord> getPledgeByVerificationToken(String token) {
        return pledges.stream()
                .filter(p -> token.equalsIgnoreCase(p.verificationToken()))
                .findFirst();
    }

    public void addPledge(PledgeRecord pledge) {
        pledges.add(pledge);
        sqliteService.savePledge(pledge);
    }

    public void updatePledge(PledgeRecord updated) {
        for (int i = 0; i < pledges.size(); i++) {
            if (pledges.get(i).id().equals(updated.id())) {
                pledges.set(i, updated);
                sqliteService.savePledge(updated);
                return;
            }
        }
        pledges.add(updated);
        sqliteService.savePledge(updated);
    }

    public List<IncidentRecord> getAllIncidents() {
        return new ArrayList<>(incidents);
    }

    public Optional<IncidentRecord> getIncidentById(String id) {
        return incidents.stream().filter(inc -> inc.id().equals(id)).findFirst();
    }

    public void addIncident(IncidentRecord incident) {
        incidents.add(incident);
        sqliteService.saveIncident(incident);
    }

    public void updateIncident(IncidentRecord updated) {
        for (int i = 0; i < incidents.size(); i++) {
            if (incidents.get(i).id().equals(updated.id())) {
                incidents.set(i, updated);
                sqliteService.saveIncident(updated);
                return;
            }
        }
        incidents.add(updated);
        sqliteService.saveIncident(updated);
    }

    public String getNextIncidentId() {
        return String.format("inc-%03d", incidentSequence.getAndIncrement());
    }

    public SqliteDatabaseService getSqliteService() {
        return sqliteService;
    }
}
