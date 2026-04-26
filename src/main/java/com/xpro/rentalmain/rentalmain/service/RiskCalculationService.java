package com.xpro.rentalmain.rentalmain.service;

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
    public CreditScore generateScore(UUID tenantId) {
        log.info("Generating credit score for tenant: {}", tenantId);

        // 1. Get the current active link for this tenant
        TenantFeatureLink activeLink = linkRepo.findByTenantIdAndIsActiveTrue(tenantId)
                .orElseThrow(() -> new RuntimeException("No active feature snapshot linked to tenant"));

        // 2. Fetch the actual feature map data
        BehavioralFeatures features = featureRepo.findById(activeLink.getFeatureSnapshotId())
                .orElseThrow(() -> new RuntimeException("Feature snapshot data not found"));

        // 3. Delegate the math to the Model (it handles weights via RiskWeightService internally)
        RiskScore result = riskModel.predict(features);

        // 4. Map the results to your persistent CreditScore entity
        CreditScore creditScore = new CreditScore();
        creditScore.setTenantId(tenantId);
        creditScore.setProbabilityOfDefault(result.getScore()); // Using raw BigDecimal from model

        // 5. Enrich with business logic (Bands and Integer scales)
        creditScore.setScore(pdToIntegerScore(result.getScore()));
        creditScore.setRiskBand(determineBand(result.getScore()));
        creditScore.setModelVersion("v1-dynamic");
        creditScore.setScoredAt(LocalDateTime.now());

        return scoreRepo.save(creditScore);
    }

    @Transactional(readOnly = true)
    public Page<CreditScore> getRankedLeaderboardPaged(int page, int size) {
        return scoreRepo.findAllLatestRankedPaged(PageRequest.of(page, size));
    }

    /**
     * Retrieves the most recently calculated score from the database.
     * Does not trigger a new calculation.
     */
    @Transactional(readOnly = true)
    public CreditScore getLatestScore(UUID tenantId) {
        log.info("Fetching latest stored score for tenant: {}", tenantId);

        return scoreRepo.findTopByTenantIdOrderByScoredAtDesc(tenantId)
                .orElseThrow(() -> new RuntimeException("No score history found for tenant: " + tenantId));
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