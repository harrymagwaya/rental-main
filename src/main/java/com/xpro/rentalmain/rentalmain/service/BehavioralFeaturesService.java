package com.xpro.rentalmain.rentalmain.service;

import com.xpro.rentalmain.rentalmain.dto.BehavioralFeatureDTO;
import com.xpro.rentalmain.rentalmain.entity.BehavioralFeatures;
import com.xpro.rentalmain.rentalmain.entity.TenantFeatureLink;
import com.xpro.rentalmain.rentalmain.repository.BehavioralFeaturesRepository;
import com.xpro.rentalmain.rentalmain.repository.TenantFeatureLinkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BehavioralFeaturesService {

    private final BehavioralFeaturesRepository featuresRepository;
    private final TenantFeatureLinkRepository linkRepository;

    // =========================
    // 1. ATTACHMENT LOGIC
    // =========================

    @Transactional
    public BehavioralFeatureDTO attachToTenant(UUID tenantId, UUID featureId) {
        // Deactivate old
        linkRepository.findByTenantIdAndIsActiveTrue(tenantId)
                .ifPresent(link -> {
                    link.setActive(false);
                    linkRepository.save(link);
                });

        // Activate new
        TenantFeatureLink newLink = new TenantFeatureLink();
        newLink.setTenantId(tenantId);
        newLink.setFeatureSnapshotId(featureId);
        newLink.setActive(true);
        linkRepository.save(newLink);

        return getById(featureId);
    }

    @Transactional
    public BehavioralFeatureDTO createFeatureSnapshot(BehavioralFeatureDTO dto) {
        BehavioralFeatures entity = new BehavioralFeatures();
        entity.setFeatureKey(dto.featureKey());
        entity.setFeatureValues(dto.featureValues());

        BehavioralFeatures saved = featuresRepository.save(entity);
        return mapToDTO(saved);
    }

    @Transactional(readOnly = true)
    public BehavioralFeatureDTO getById(UUID id) {
        return featuresRepository.findById(id).map(this::mapToDTO).orElseThrow();
    }

    @Transactional(readOnly = true)
    public List<BehavioralFeatureDTO> getAll() {
        return featuresRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    private BehavioralFeatureDTO mapToDTO(BehavioralFeatures entity) {
        return new BehavioralFeatureDTO(entity.getId(), entity.getFeatureKey(), entity.getFeatureValues());
    }
    @Transactional
    public void delete(UUID id) {
        // Warning: Check if any TenantFeatureLink depends on this before deleting
        featuresRepository.deleteById(id);
        log.info("Deleted feature snapshot: {}", id);
    }
}