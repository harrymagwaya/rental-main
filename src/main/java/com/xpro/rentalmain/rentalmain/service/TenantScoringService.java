package com.xpro.rentalmain.rentalmain.service;

import com.xpro.rentalmain.rentalmain.entity.BehavioralFeatures;
import com.xpro.rentalmain.rentalmain.entity.TenantFeatureLink;
import com.xpro.rentalmain.rentalmain.model.RiskScore;
import com.xpro.rentalmain.rentalmain.repository.BehavioralFeaturesRepository;
import com.xpro.rentalmain.rentalmain.repository.TenantFeatureLinkRepository;
import com.xpro.rentalmain.rentalmain.service.model.CreditRiskModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TenantScoringService {

    private final TenantFeatureLinkRepository linkRepository;
    private final BehavioralFeaturesRepository featuresRepository;
    private final CreditRiskModel creditRiskModel;

    @Transactional(readOnly = true)
    public RiskScore calculateTenantScore(UUID tenantId) {
        // Get the active link
        TenantFeatureLink link = linkRepository.findByTenantIdAndIsActiveTrue(tenantId)
                .orElseThrow(() -> new RuntimeException("No active feature link for tenant: " + tenantId));

        // Load the snapshot
        BehavioralFeatures features = featuresRepository.findById(link.getFeatureSnapshotId())
                .orElseThrow(() -> new RuntimeException("Feature snapshot data missing"));

        // Generate score via Math Engine (Cache + Entity Map)
        return creditRiskModel.predict(features);
    }
}