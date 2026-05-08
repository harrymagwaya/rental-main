package com.xpro.rentalmain.rentalmain.service.model;//package com.xpro.rentalmain.rentalmain.service.model;
//
//import com.xpro.rentalmain.rentalmain.entity.BehavioralFeatures;
//import com.xpro.rentalmain.rentalmain.model.RiskCategory;
//import com.xpro.rentalmain.rentalmain.model.RiskScore;
//import com.xpro.rentalmain.rentalmain.service.RiskWeightService;
//import org.springframework.stereotype.Service;
//
//import java.math.BigDecimal;
//import java.util.Map;
//
//@Service
//public class CreditRiskModel {
//
//    // Now communicating exclusively with the Service layer
//    private final RiskWeightService weightService;
//
//    public CreditRiskModel(RiskWeightService weightService) {
//        this.weightService = weightService;
//    }
//
//    public RiskScore predict(BehavioralFeatures features) {
//        BigDecimal totalRisk = BigDecimal.ZERO;
//
//        totalRisk = totalRisk.add(safeMultiply(features.getRentConsistency(), weightService.getActiveWeight("RENT_CONSISTENCY")));
//        totalRisk = totalRisk.add(safeMultiply(features.getUtilityPayments(), weightService.getActiveWeight("UTILITY_PAYMENTS")));
//        totalRisk = totalRisk.add(safeMultiply(features.getAirtimeUsage(), weightService.getActiveWeight("AIRTIME_USAGE")));
//        totalRisk = totalRisk.add(safeMultiply(features.getSavingsConsistency(), weightService.getActiveWeight("SAVINGS_CONSISTENCY")));
//        totalRisk = totalRisk.add(safeMultiply(features.getLoanRepaymentRate(), weightService.getActiveWeight("LOAN_REPAYMENT")));
//        totalRisk = totalRisk.add(safeMultiply(features.getMobileMoneyVolume(), weightService.getActiveWeight("MOMO_VOLUME")));
//        totalRisk = totalRisk.add(safeMultiply(features.getTransactionDiversity(), weightService.getActiveWeight("TX_DIVERSITY")));
//        totalRisk = totalRisk.add(safeMultiply(features.getLengthOfResidence(), weightService.getActiveWeight("RESIDENCE_STABILITY")));
//
//        return new RiskScore(totalRisk, classify(totalRisk));
//    }
//
//    private BigDecimal safeMultiply(BigDecimal value, BigDecimal weight) {
//        if (value == null || weight == null) {
//            return BigDecimal.ZERO;
//        }
//        return value.multiply(weight);
//    }
//
////    private RiskCategory classify(BigDecimal score) {
////        if (score.compareTo(new BigDecimal("0.3")) < 0) return RiskCategory.LOW_RISK;
////        if (score.compareTo(new BigDecimal("0.6")) < 0) return RiskCategory.MEDIUM_RISK;
////        return RiskCategory.HIGH_RISK;
////    }
//
//    private RiskCategory classify(BigDecimal score) {
//        // 0.7 to 1.0 = Great Performance -> LOW RISK
//        if (score.compareTo(new BigDecimal("0.7")) >= 0) {
//            return RiskCategory.LOW_RISK;
//        }
//        // 0.4 to 0.69 = Okay Performance -> MEDIUM RISK
//        if (score.compareTo(new BigDecimal("0.4")) >= 0) {
//            return RiskCategory.MEDIUM_RISK;
//        }
//        // 0.0 to 0.39 = Poor Performance -> HIGH RISK
//        return RiskCategory.HIGH_RISK;
//    }
//}

import com.xpro.rentalmain.rentalmain.entity.BehavioralFeatures;
import com.xpro.rentalmain.rentalmain.model.FeatureKey;
import com.xpro.rentalmain.rentalmain.model.RiskCategory;
import com.xpro.rentalmain.rentalmain.model.RiskScore;
import com.xpro.rentalmain.rentalmain.service.RiskWeightService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
@RequiredArgsConstructor
@Slf4j
public class CreditRiskModel {

    private final RiskWeightService weightService; // Fetches weights from DB by Enum

    public RiskScore predict(BehavioralFeatures features) {
        BigDecimal weightedSum = BigDecimal.ZERO;
        BigDecimal totalWeightUsed = BigDecimal.ZERO;

        // Map Enum keys to the actual values in the entity
        Map<FeatureKey, BigDecimal> featureValues = Map.of(
                FeatureKey.RENT_CONSISTENCY, features.getRentConsistency(),
                FeatureKey.UTILITY_PAYMENTS, features.getUtilityPayments(),
                FeatureKey.AIRTIME_USAGE, features.getAirtimeUsage(),
                FeatureKey.SAVINGS_CONSISTENCY, features.getSavingsConsistency(),
                FeatureKey.LOAN_REPAYMENT, features.getLoanRepaymentRate(),
                FeatureKey.MOMO_VOLUME, features.getMobileMoneyVolume(),
                FeatureKey.TX_DIVERSITY, features.getTransactionDiversity(),
                FeatureKey.RESIDENCE_STABILITY, features.getLengthOfResidence()
        );

        for (var entry : featureValues.entrySet()) {
            BigDecimal val = entry.getValue();
            if (val != null) {
                // Get weight using the Enum name (SNAKE_CASE)
                BigDecimal weight = weightService.getActiveWeight(entry.getKey().name());
                weightedSum = weightedSum.add(val.multiply(weight));
                totalWeightUsed = totalWeightUsed.add(weight);
            }
        }

        // NORMALIZATION: Scale back to 1.0 if some data was missing
        BigDecimal finalScore = (totalWeightUsed.compareTo(BigDecimal.ZERO) > 0)
                ? weightedSum.divide(totalWeightUsed, 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return new RiskScore(finalScore, classify(finalScore));
    }

    /**
     * CLASSIFIER: High Performance (0.8+) = LOW RISK
     */
    private RiskCategory classify(BigDecimal score) {
        if (score.compareTo(new BigDecimal("0.75")) >= 0) return RiskCategory.LOW_RISK;
        if (score.compareTo(new BigDecimal("0.45")) >= 0) return RiskCategory.MEDIUM_RISK;
        return RiskCategory.HIGH_RISK;
    }
}