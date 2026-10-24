package com.rippleready.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rippleready.model.DependentFacility;
import com.rippleready.service.DataStore;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/facilities")
@CrossOrigin(origins = "*")
@Tag(name = "Community Facilities", description = "Registry of dependent institutions (crèches, frail care, food merchants)")
public class FacilityController {

    private final DataStore dataStore;

    public FacilityController(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    @GetMapping
    @Operation(summary = "List all registered dependent facilities")
    public ResponseEntity<List<DependentFacility>> listFacilities() {
        return ResponseEntity.ok(dataStore.getAllFacilities());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get facility details by ID")
    public ResponseEntity<DependentFacility> getFacility(@PathVariable String id) {
        return dataStore.getFacilityById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Register a new vulnerable or dependent community node")
    public ResponseEntity<DependentFacility> registerFacility(@RequestBody DependentFacility facility) {
        String id = (facility.id() != null && !facility.id().isBlank())
                ? facility.id()
                : "fac-" + UUID.randomUUID().toString().substring(0, 8);

        DependentFacility created = new DependentFacility(
                id,
                facility.name(),
                facility.type(),
                facility.latitude(),
                facility.longitude(),
                facility.address(),
                facility.contactPerson(),
                facility.contactPhone(),
                facility.beneficiaryCount(),
                facility.bufferHours(),
                facility.primaryNeed(),
                facility.estimatedDailyLossZar(),
                true
        );

        dataStore.addFacility(created);
        return ResponseEntity.ok(created);
    }
}

