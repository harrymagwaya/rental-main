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

    /**
     * The Public Method called by your Controller to generate and persist a score.
     */
    @Transactional
    public RiskScoreResponseDTO generateScore(UUID tenantId) { // Changed return type
        log.info("Generating credit score for tenant: {}", tenantId);

        TenantFeatureLink activeLink = linkRepo.findByTenantIdAndIsActiveTrue(tenantId)
                .orElseThrow(() -> new RuntimeException("No active feature snapshot linked to tenant"));

        BehavioralFeatures features = featureRepo.findById(activeLink.getFeatureSnapshotId())
                .orElseThrow(() -> new RuntimeException("Feature snapshot data not found"));

        RiskScore result = riskModel.predict(features);

        CreditScore creditScore = new CreditScore();
        creditScore.setTenantId(tenantId);
        creditScore.setProbabilityOfDefault(result.getScore());
        creditScore.setRiskCategory(result.getCategory()); 
        creditScore.setScore(pdToIntegerScore(result.getScore()));
        creditScore.setRiskBand(determineBand(result.getScore()));
        creditScore.setModelVersion("v1-dynamic");
        creditScore.setScoredAt(LocalDateTime.now());

        CreditScore saved = scoreRepo.save(creditScore);

        // Return the clean DTO
        return new RiskScoreResponseDTO(
                saved.getTenantId(),
                saved.getProbabilityOfDefault(),
                saved.getRiskCategory(),
                saved.getScore(),
                saved.getRiskBand(),
                saved.getModelVersion(),
                saved.getScoredAt()
        );
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

    /**
     * Centralized mapping logic to ensure consistency across all methods.
     */
    private RiskScoreResponseDTO mapToResponseDTO(CreditScore entity) {
        return new RiskScoreResponseDTO(
                entity.getTenantId(),
                entity.getProbabilityOfDefault(),
                entity.getRiskCategory(), // Now safely stored in your entity
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

    private RiskBand determineBand(BigDecimal pd) {
        if (pd.compareTo(new BigDecimal("0.20")) < 0) return RiskBand.PLATINUM;
        if (pd.compareTo(new BigDecimal("0.40")) < 0) return RiskBand.GOLD;
        if (pd.compareTo(new BigDecimal("0.60")) < 0) return RiskBand.SILVER;
        return RiskBand.BRONZE;
    }
}