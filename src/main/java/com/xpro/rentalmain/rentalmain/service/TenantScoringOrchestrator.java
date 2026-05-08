package com.xpro.rentalmain.rentalmain.service;

import com.xpro.rentalmain.rentalmain.dto.BehavioralFeatureDTO;
import com.xpro.rentalmain.rentalmain.dto.BehavioralFeatureRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantScoringOrchestrator {

    private final RiskFeatureProcessor featureProcessor;
    private final BehavioralFeaturesService behavioralService;
    private final RiskCalculationService riskService;
    private final EligibilityService eligibilityService;

    /**
     * THE MASTER PIPELINE:
     * Runs the entire calculation flow from raw data to final eligibility limits.
     */
    @Transactional
    public void refreshTenantFinancialProfile(UUID tenantId) {
        log.info("Starting master scoring pipeline for tenant: {}", tenantId);

        // 1. ACCOUNTANT: Read raw history and calculate percentages
        BehavioralFeatureRequestDTO featuresDto = featureProcessor.engineerFeatures(tenantId);

        // 2. SNAPSHOT: Save the new percentages as a historical record
        BehavioralFeatureDTO snapshot = behavioralService.createFeatureSnapshot(featuresDto);

        // 3. LINKER: Attach this new snapshot as the "Active" one
        behavioralService.attachToTenant(tenantId, snapshot.id());

        // 4. MATH ENGINE: Calculate the new 0-900 score and save it
        riskService.generateScore(tenantId);

        // 5. GATEKEEPER: Recalculate borrowing limits based on the new score
        eligibilityService.processFullEligibility(tenantId);

        log.info("Master scoring pipeline completed successfully for tenant: {}", tenantId);
    }
}