package com.xpro.rentalmain.rentalmain.controller;

import com.xpro.rentalmain.rentalmain.dto.BehavioralFeatureDTO;
import com.xpro.rentalmain.rentalmain.service.BehavioralFeaturesService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/behavioral-features")
@RequiredArgsConstructor
public class BehavioralFeaturesController {

    private final BehavioralFeaturesService featuresService;

    // Create a new data snapshot
    @PostMapping
    public BehavioralFeatureDTO create(@RequestBody BehavioralFeatureDTO dto) {
        return featuresService.createFeatureSnapshot(dto);
    }

    // Get all available snapshots
    @GetMapping
    public List<BehavioralFeatureDTO> listAll() {
        return featuresService.getAll();
    }

    // Get specific snapshot by ID
    @GetMapping("/{id}")
    public BehavioralFeatureDTO getOne(@PathVariable UUID id) {
        return featuresService.getById(id);
    }

    // Attach an existing snapshot to a tenant (Updates the Link table)
    @PostMapping("/{featureId}/attach-to/{tenantId}")
    public BehavioralFeatureDTO attach(@PathVariable UUID featureId, @PathVariable UUID tenantId) {
        return featuresService.attachToTenant(tenantId, featureId);
    }

    // Delete a snapshot
    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        featuresService.delete(id);
    }
}