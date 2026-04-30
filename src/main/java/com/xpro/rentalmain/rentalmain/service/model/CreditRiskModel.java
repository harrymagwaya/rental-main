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

        totalRisk = totalRisk.add(safeMultiply(features.getRentConsistency(), weightService.getActiveWeight("RENT_CONSISTENCY")));
        totalRisk = totalRisk.add(safeMultiply(features.getUtilityPayments(), weightService.getActiveWeight("UTILITY_PAYMENTS")));
        totalRisk = totalRisk.add(safeMultiply(features.getAirtimeUsage(), weightService.getActiveWeight("AIRTIME_USAGE")));
        totalRisk = totalRisk.add(safeMultiply(features.getSavingsConsistency(), weightService.getActiveWeight("SAVINGS_CONSISTENCY")));
        totalRisk = totalRisk.add(safeMultiply(features.getLoanRepaymentRate(), weightService.getActiveWeight("LOAN_REPAYMENT")));
        totalRisk = totalRisk.add(safeMultiply(features.getMobileMoneyVolume(), weightService.getActiveWeight("MOMO_VOLUME")));
        totalRisk = totalRisk.add(safeMultiply(features.getTransactionDiversity(), weightService.getActiveWeight("TX_DIVERSITY")));
        totalRisk = totalRisk.add(safeMultiply(features.getLengthOfResidence(), weightService.getActiveWeight("RESIDENCE_STABILITY")));

        return new RiskScore(totalRisk, classify(totalRisk));
    }

    private BigDecimal safeMultiply(BigDecimal value, BigDecimal weight) {
        if (value == null || weight == null) {
            return BigDecimal.ZERO;
        }
        return value.multiply(weight);
    }

    private RiskCategory classify(BigDecimal score) {
        if (score.compareTo(new BigDecimal("0.3")) < 0) return RiskCategory.LOW_RISK;
        if (score.compareTo(new BigDecimal("0.6")) < 0) return RiskCategory.MEDIUM_RISK;
        return RiskCategory.HIGH_RISK;
    }
}