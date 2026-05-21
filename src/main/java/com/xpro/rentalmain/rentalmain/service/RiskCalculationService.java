package com.xpro.rentalmain.rentalmain.service;

import com.xpro.rentalmain.rentalmain.dto.RiskScoreResponseDTO;
import com.xpro.rentalmain.rentalmain.entity.BehavioralFeatures;
import com.xpro.rentalmain.rentalmain.entity.CreditScore;
import com.xpro.rentalmain.rentalmain.entity.TenantFeatureLink;
import com.xpro.rentalmain.rentalmain.model.RiskBand;
import com.xpro.rentalmain.rentalmain.model.RiskScore;
import com.xpro.rentalmain.rentalmain.repository.BehavioralFeaturesRepository;
import com.xpro.rentalmain.rentalmain.repository.CreditScoreRepository;
import com.xpro.rentalmain.rentalmain.repository.TenantFeatureLinkRepository;
import com.xpro.rentalmain.rentalmain.service.model.CreditRiskModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RiskCalculationService {

    private final BehavioralFeaturesRepository featureRepo;
    private final TenantFeatureLinkRepository linkRepo;
    private final CreditScoreRepository scoreRepo;
    private final CreditRiskModel riskModel; // Our Math Engine

    @Transactional(readOnly = true)
    public List<RiskScoreResponseDTO> getAllSavedScores() {
        log.info("Fetching all saved credit scores from database");

        // Assuming you want the most recent score for every user
        List<CreditScore> allScores = scoreRepo.findAll();

        return allScores.stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    /**
     * BATCH PROCESS: Iterates through all active tenants and refreshes their scores.
     * This is designed to be called by the Scheduler or an Admin trigger.
     */
    @Transactional
    public void runGlobalBatchScoring() {
        log.info("Starting global batch scoring process...");

        // Use the repository method we refined to only get relevant tenants
        List<UUID> activeTenantIds = linkRepo.findAllActiveTenantIds();

        int successCount = 0;
        for (UUID tenantId : activeTenantIds) {
            try {
                // Reuse your existing logic
                this.generateScore(tenantId);
                successCount++;
            } catch (Exception e) {
                log.error("Failed to calculate batch score for tenant {}: {}", tenantId, e.getMessage());
            }
        }

        log.info("Batch scoring completed. Successfully updated {}/{} tenants.",
                successCount, activeTenantIds.size());
    }
    @Transactional(readOnly = true)
    public RiskScore calculateTenantRiskScore(UUID tenantId) {
        // Get the active link
        TenantFeatureLink link = linkRepo.findByTenantIdAndIsActiveTrue(tenantId)
                .orElseThrow(() -> new RuntimeException("No active feature link for tenant: " + tenantId));

        // Load the snapshot
        BehavioralFeatures features = featureRepo.findById(link.getFeatureSnapshotId())
                .orElseThrow(() -> new RuntimeException("Feature snapshot data missing"));

        // Generate score via Math Engine (Cache + Entity Map)
        return riskModel.predict(features);
    }
    @Transactional
    public RiskScoreResponseDTO generateScore(UUID tenantId) {
        log.info("Generating credit score for tenant: {}", tenantId);

        TenantFeatureLink activeLink = linkRepo.findByTenantIdAndIsActiveTrue(tenantId)
                .orElseThrow(() -> new RuntimeException("No active feature snapshot linked to tenant"));

        BehavioralFeatures features = featureRepo.findById(activeLink.getFeatureSnapshotId())
                .orElseThrow(() -> new RuntimeException("Feature snapshot data not found"));

        // 1. Get dual-metric result from the Math Engine
        RiskScore result = riskModel.predict(features);

        // 2. Prepare the Entity with both Success Rate and PD
        CreditScore creditScore = new CreditScore();
        creditScore.setTenantId(tenantId);
        creditScore.setSuccessRate(result.getSuccessRate());         // Added
        creditScore.setProbabilityOfDefault(result.getProbabilityOfDefault()); // Corrected
        creditScore.setRiskCategory(result.getCategory());

        // Use Success Rate for the integer score calculation (Higher is better)
        creditScore.setScore(calculateIntegerScore(result.getSuccessRate()));
        creditScore.setRiskBand(determineBand(result.getProbabilityOfDefault()));

        creditScore.setModelVersion("v1-dynamic");
        creditScore.setScoredAt(LocalDateTime.now());

        CreditScore saved = scoreRepo.save(creditScore);

        return mapToResponseDTO(saved);
    }
    @Transactional(readOnly = true)
    public Page<RiskScoreResponseDTO> getRankedLeaderboardPaged(int page, int size) {
        log.info("Fetching ranked leaderboard - Page: {}, Size: {}", page, size);

        Page<CreditScore> scores = scoreRepo.findAllLatestRankedPaged(PageRequest.of(page, size));

        // Use Page.map to convert each entity to a DTO
        return scores.map(this::mapToResponseDTO);
    }

    /**
     * Retrieves the most recently calculated score from the database.
     * Does not trigger a new calculation.
     */
    @Transactional(readOnly = true)
    public RiskScoreResponseDTO getLatestScore(UUID tenantId) {
        log.info("Fetching latest stored score for tenant: {}", tenantId);

        CreditScore latest = scoreRepo.findTopByTenantIdOrderByScoredAtDesc(tenantId)
                .orElseThrow(() -> {
                    log.error("No score history found for tenant: {}", tenantId);
                    return new RuntimeException("No score history found for tenant: " + tenantId);
                });

        return mapToResponseDTO(latest);
    }

    private RiskScoreResponseDTO mapToResponseDTO(CreditScore entity) {
        return new RiskScoreResponseDTO(
                entity.getId(),
                entity.getTenantId(),
                entity.getSuccessRate(),          // Performance
                entity.getProbabilityOfDefault(), // Risk
                entity.getRiskCategory(),
                entity.getScore(),
                entity.getRiskBand(),
                entity.getModelVersion(),
                entity.getScoredAt()
        );
    }
    private Integer pdToIntegerScore(BigDecimal pd) {
        // scale: (1 - PD) * 900 -> 0 PD = 900 score, 1 PD = 0 score
        return BigDecimal.ONE.subtract(pd)
                .multiply(new BigDecimal("900"))
                .intValue();
    }

    private Integer calculateIntegerScore(BigDecimal successRate) {
        // SuccessRate * 900 -> 1.0 = 900, 0.0 = 0
        return successRate.multiply(new BigDecimal("900")).intValue();
    }

    private RiskBand determineBand(BigDecimal pd) {
        if (pd.compareTo(new BigDecimal("0.20")) < 0) return RiskBand.PLATINUM;
        if (pd.compareTo(new BigDecimal("0.40")) < 0) return RiskBand.GOLD;
        if (pd.compareTo(new BigDecimal("0.60")) < 0) return RiskBand.SILVER;
        return RiskBand.BRONZE;
    }
}