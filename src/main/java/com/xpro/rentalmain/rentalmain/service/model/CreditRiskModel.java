package com.xpro.rentalmain.rentalmain.service.model;

import com.xpro.rentalmain.rentalmain.entity.BehavioralFeatures;
import com.xpro.rentalmain.rentalmain.model.RiskCategory;
import com.xpro.rentalmain.rentalmain.model.RiskScore;
import com.xpro.rentalmain.rentalmain.service.RiskWeightService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class CreditRiskModel {

    // Now communicating exclusively with the Service layer
    private final RiskWeightService weightService;

    public CreditRiskModel(RiskWeightService weightService) {
        this.weightService = weightService;
    }

    public RiskScore predict(BehavioralFeatures features) {
        BigDecimal totalRisk = BigDecimal.ZERO;

        Map<String, BigDecimal> featureValues = features.getFeatureValues();

        if (featureValues == null || featureValues.isEmpty()) {
            return new RiskScore(totalRisk, classify(totalRisk));
        }

        for (Map.Entry<String, BigDecimal> entry : featureValues.entrySet()) {
            String featureKey = entry.getKey();
            BigDecimal featureValue = entry.getValue();

            // The Service internally decides whether to hit Redis or the DB
            BigDecimal weight = weightService.getActiveWeight(featureKey);

            if (featureValue != null && weight != null) {
                totalRisk = totalRisk.add(featureValue.multiply(weight));
            }
        }

        return new RiskScore(totalRisk, classify(totalRisk));
    }

    private RiskCategory classify(BigDecimal score) {
        if (score.compareTo(new BigDecimal("2.5")) < 0) return RiskCategory.LOW_RISK;
        if (score.compareTo(new BigDecimal("5.0")) < 0) return RiskCategory.MEDIUM_RISK;
        return RiskCategory.HIGH_RISK;
    }
}