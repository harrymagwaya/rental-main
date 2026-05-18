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

        log.info("STEP 1 - Engineering features");
        // 1. ACCOUNTANT: Read raw history and calculate percentages
        BehavioralFeatureRequestDTO featuresDto = featureProcessor.engineerFeatures(tenantId);

        log.info("STEP 2 - Creating snapshot");
        // 2. SNAPSHOT: Save the new percentages as a historical record
        BehavioralFeatureDTO snapshot = behavioralService.createFeatureSnapshot(featuresDto);

        log.info("STEP 3 - Attaching snapshot");
        // 3. LINKER: Attach this new snapshot as the "Active" one
        behavioralService.attachToTenant(tenantId, snapshot.id());

        log.info("STEP 4 - Generating score");
        // 4. MATH ENGINE: Calculate the new 0-900 score and save it
        riskService.generateScore(tenantId);

        log.info("STEP 5 - Processing eligibility");
        // 5. GATEKEEPER: Recalculate borrowing limits based on the new score
        eligibilityService.processFullEligibility(tenantId);

        log.info("Master scoring pipeline completed successfully for tenant: {}", tenantId);
    }
}