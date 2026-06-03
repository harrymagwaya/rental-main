package com.xpro.rentalmain.rentalmain.service;

import com.xpro.rentalmain.rentalmain.dto.BehavioralFeatureDTO;
import com.xpro.rentalmain.rentalmain.dto.BehavioralFeatureRequestDTO;
import com.xpro.rentalmain.rentalmain.entity.BehavioralFeatures;
import com.xpro.rentalmain.rentalmain.entity.TenantFeatureLink;
import com.xpro.rentalmain.rentalmain.repository.BehavioralFeaturesRepository;
import com.xpro.rentalmain.rentalmain.repository.TenantFeatureLinkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    public void initializeDefaultTenantFeatures(UUID tenantId) {
        log.info("Provisioning fresh baseline behavioral footprint for new tenant: {}", tenantId);

        // 1. Create default behavioral tracking values (ground-zero metrics)
        BehavioralFeatures baselineEntity = new BehavioralFeatures();
        baselineEntity.setRentConsistency(BigDecimal.ZERO);
        baselineEntity.setUtilityPayments(BigDecimal.ZERO);
        baselineEntity.setAirtimeUsage(BigDecimal.ZERO);
        baselineEntity.setSavingsConsistency(BigDecimal.ZERO);
        baselineEntity.setLoanRepaymentRate(BigDecimal.ZERO);
        baselineEntity.setMobileMoneyVolume(BigDecimal.ZERO);
        baselineEntity.setTransactionDiversity(BigDecimal.ZERO);
        baselineEntity.setLengthOfResidence(BigDecimal.ZERO);

        BehavioralFeatures savedSnapshot = featuresRepository.save(baselineEntity);
        log.debug("Saved baseline BehavioralFeatures snapshot with ID: {}", savedSnapshot.getId());

        // 2. Explicitly map and activate the link within the current transaction session
        TenantFeatureLink activeLink = new TenantFeatureLink();
        activeLink.setTenantId(tenantId);
        activeLink.setFeatureSnapshotId(savedSnapshot.getId());
        activeLink.setActive(true); // Explicitly setting active state

        TenantFeatureLink savedLink = linkRepository.save(activeLink);
        log.info("Successfully bound tenant [{}] to initial active tracking link [ID: {}]", tenantId, savedLink.getId());

    }

    @Transactional
    public BehavioralFeatureDTO attachToTenant(UUID tenantId, UUID featureId) {
        log.info("Starting attachment of feature snapshot [{}] to tenant [{}]", featureId, tenantId);

        // Deactivate old links to ensure only one snapshot is "Active" at a time
        linkRepository.findByTenantIdAndIsActiveTrue(tenantId)
                .ifPresent(link -> {
                    log.debug("Deactivating existing active link [ID: {}] for tenant [{}]", link.getId(), tenantId);
                    link.setActive(false);
                    linkRepository.save(link);
                });

        // Activate new link
        TenantFeatureLink newLink = new TenantFeatureLink();
        newLink.setTenantId(tenantId);
        newLink.setFeatureSnapshotId(featureId);
        newLink.setActive(true);

        TenantFeatureLink savedLink = linkRepository.save(newLink);
        log.info("Successfully activated new feature link [ID: {}] for tenant [{}]", savedLink.getId(), tenantId);

        return getById(featureId);
    }

    @Transactional
    public BehavioralFeatureDTO createFeatureSnapshot(BehavioralFeatureRequestDTO dto) {
        log.info("Creating new static behavioral snapshot. name: {}", dto.snapshotVersion());

        BehavioralFeatures entity = new BehavioralFeatures();

        // Explicitly mapping fields
        entity.setRentConsistency(dto.rentConsistency());
        entity.setUtilityPayments(dto.utilityPayments());
        entity.setAirtimeUsage(dto.airtimeUsage());
        entity.setSavingsConsistency(dto.savingsConsistency());
        entity.setLoanRepaymentRate(dto.loanRepaymentRate());
        entity.setMobileMoneyVolume(dto.mobileMoneyVolume());
        entity.setTransactionDiversity(dto.transactionDiversity());
        entity.setLengthOfResidence(dto.lengthOfResidence());

        BehavioralFeatures saved = featuresRepository.save(entity);
        log.debug("Saved BehavioralFeatures entity with ID: {}", saved.getId());

        return mapToDTO(saved);
    }

    @Transactional(readOnly = true)
    public BehavioralFeatureDTO getById(UUID id) {
        log.debug("Fetching behavioral snapshot by ID: {}", id);
        return featuresRepository.findById(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> {
                    log.error("Feature snapshot NOT FOUND for ID: {}", id);
                    return new RuntimeException("Snapshot not found: " + id);
                });
    }

    @Transactional(readOnly = true)
    public List<BehavioralFeatureDTO> getAll() {
        log.info("Retrieving all behavioral snapshots from database");
        List<BehavioralFeatures> allFeatures = featuresRepository.findAll();
        log.debug("Found {} snapshots total", allFeatures.size());

        return allFeatures.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private BehavioralFeatureDTO mapToDTO(BehavioralFeatures entity) {
        return new BehavioralFeatureDTO(
                entity.getId(),
                entity.getRentConsistency(),
                entity.getUtilityPayments(),
                entity.getAirtimeUsage(),
                entity.getSavingsConsistency(),
                entity.getLoanRepaymentRate(),
                entity.getMobileMoneyVolume(),
                entity.getTransactionDiversity(),
                entity.getLengthOfResidence()
        );
    }

    @Transactional
    public void delete(UUID id) {
        log.warn("ATTEMPTING DELETE of feature snapshot ID: {}", id);

        // Safety check: Don't delete snapshots that are currently linked to tenants
        if (linkRepository.existsByFeatureSnapshotId(id)) {
            log.error("ABORTING DELETE: Snapshot [{}] is currently linked to one or more tenants.", id);
            throw new RuntimeException("Cannot delete snapshot; it is currently in use.");
        }

        featuresRepository.deleteById(id);
        log.info("Successfully deleted feature snapshot ID: {}", id);
    }
}