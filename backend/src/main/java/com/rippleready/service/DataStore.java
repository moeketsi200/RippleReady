package com.rippleready.service;

import com.rippleready.model.*;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class DataStore {

    private final Map<String, DependentFacility> facilities = new ConcurrentHashMap<>();
    private final Map<String, MutualAidResource> resources = new ConcurrentHashMap<>();
    private final List<PledgeRecord> pledges = new CopyOnWriteArrayList<>();
    private final List<IncidentRecord> incidents = new CopyOnWriteArrayList<>();
    private final AtomicInteger incidentSequence = new AtomicInteger(1);

    public DataStore() {
        seedInitialData();
    }

    public void reset() {
        facilities.clear();
        resources.clear();
        pledges.clear();
        incidents.clear();
        incidentSequence.set(1);
        seedInitialData();
    }

    private void seedInitialData() {
        // High-Priority Community Anchors (George, Western Cape / Rosemoor / Pacaltsdorp corridor)
        addFacility(new DependentFacility(
                "fac-1",
                "Little Stars Crèche",
                FacilityType.CRECHE,
                -33.9632, 22.4612,
                "42 Beer St, Rosemoor, George",
                "Sis' Thandiwe",
                "+27 82 555 4101",
                40,
                2, // 2-hour water buffer
                "Drinking water for 40 toddlers & sanitation",
                3200.0,
                true,
                "THREATENED"
        ));

        addFacility(new DependentFacility(
                "fac-2",
                "Mama K's Spaza & Corner Eatery",
                FacilityType.FOOD_MERCHANT,
                -33.9615, 22.4640,
                "18 Woltemade St, Rosemoor, George",
                "Mama Khanyisa",
                "+27 73 555 7720",
                120, // 120 lunch patrons
                3, // 3-hour buffer before lunch prep halts
                "Sanitation & food preparation water",
                4800.0,
                true,
                "STABLE"
        ));

        addFacility(new DependentFacility(
                "fac-3",
                "Rosemoor Fragile & Elderly Care",
                FacilityType.ELDERLY_CARE,
                -33.9590, 22.4580,
                "5 Attaqua Way, Rosemoor, George",
                "Nurse Matron Jacobs",
                "+27 84 555 3391",
                28,
                1, // 1-hour buffer (critical medical hygiene)
                "Medical sanitation & sterile drinking water",
                8500.0,
                true,
                "STABLE"
        ));

        addFacility(new DependentFacility(
                "fac-4",
                "St. Mark's Community Soup Kitchen",
                FacilityType.SOUP_KITCHEN,
                -33.9650, 22.4600,
                "Cathedral Lane, George Central",
                "Father Joseph",
                "+27 82 555 9012",
                200, // 200 meals
                2,
                "250L cooking water for midday meal distribution",
                2500.0,
                true,
                "STABLE"
        ));

        addFacility(new DependentFacility(
                "fac-5",
                "York Street Commercial Laundromat & SME Hub",
                FacilityType.LOCAL_SME,
                -33.9605, 22.4625,
                "104 York St, George Central",
                "Dawie van der Merwe",
                "+27 83 555 1198",
                15,
                4,
                "Utility washing water to keep Speedpoint trade active",
                6200.0,
                true,
                "STABLE"
        ));

        // Distant facility outside immediate 2.0 km perimeter (5.5 km away)
        addFacility(new DependentFacility(
                "fac-hosp-central",
                "George Central Hospital",
                FacilityType.CLINIC,
                -33.9900, 22.5000,
                "Davidson Rd, George",
                "Dr. Petersen",
                "+27 44 555 2000",
                350,
                12, // 12-hour municipal reserve
                "Secondary emergency backup connection",
                25000.0,
                true,
                "STABLE"
        ));

        // Available Peer & Community Resources
        addResource(new MutualAidResource(
                "res-1",
                "Hendricks Hardware & Agri Supplies (Borehole)",
                ResourceType.BOREHOLE_NON_POTABLE,
                -33.9580, 22.4650,
                "77 Hibernia St, George",
                "Oom Pieter Hendricks",
                "+27 82 555 0192",
                1500.0,
                1500.0, // 1500L available
                "Litres",
                true,
                true
        ));

        addResource(new MutualAidResource(
                "res-2",
                "St. Mark's Church Reservoir (Treated JoJo Tank)",
                ResourceType.JOJO_POTABLE,
                -33.9640, 22.4590,
                "St Mark St, George Central",
                "Deacon Andrew",
                "+27 83 555 0143",
                5000.0,
                5000.0,
                "Litres",
                true,
                false
        ));

        addResource(new MutualAidResource(
                "res-3",
                "Garden Route Volunteer Bowser Unit #1",
                ResourceType.MOBILE_BOWSER,
                -33.9610, 22.4550,
                "Depot 4, Industrial Area, George",
                "Sipho Mthembu",
                "+27 79 555 0122",
                2000.0,
                2000.0,
                "Litres",
                true,
                false
        ));
    }

    public List<DependentFacility> getAllFacilities() {
        return new ArrayList<>(facilities.values());
    }

    public Optional<DependentFacility> getFacilityById(String id) {
        return Optional.ofNullable(facilities.get(id));
    }

    public void addFacility(DependentFacility facility) {
        facilities.put(facility.id(), facility);
    }

    public void updateFacility(DependentFacility facility) {
        facilities.put(facility.id(), facility);
    }

    public List<MutualAidResource> getAllResources() {
        return new ArrayList<>(resources.values());
    }

    public Optional<MutualAidResource> getResourceById(String id) {
        return Optional.ofNullable(resources.get(id));
    }

    public void addResource(MutualAidResource resource) {
        resources.put(resource.id(), resource);
    }

    public void updateResource(MutualAidResource resource) {
        resources.put(resource.id(), resource);
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
    }

    public void updatePledge(PledgeRecord updated) {
        for (int i = 0; i < pledges.size(); i++) {
            if (pledges.get(i).id().equals(updated.id())) {
                pledges.set(i, updated);
                return;
            }
        }
        pledges.add(updated);
    }

    public List<IncidentRecord> getAllIncidents() {
        return new ArrayList<>(incidents);
    }

    public Optional<IncidentRecord> getIncidentById(String id) {
        return incidents.stream().filter(inc -> inc.id().equals(id)).findFirst();
    }

    public void addIncident(IncidentRecord incident) {
        incidents.add(incident);
    }

    public void updateIncident(IncidentRecord updated) {
        for (int i = 0; i < incidents.size(); i++) {
            if (incidents.get(i).id().equals(updated.id())) {
                incidents.set(i, updated);
                return;
            }
        }
        incidents.add(updated);
    }

    public String getNextIncidentId() {
        return String.format("inc-%03d", incidentSequence.getAndIncrement());
    }
}
