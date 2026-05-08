package com.xpro.rentalmain.rentalmain.service;

import com.xpro.rentalmain.rentalmain.dto.BehavioralFeatureRequestDTO;
import com.xpro.rentalmain.rentalmain.dto.FinancialRecordResponse;
import com.xpro.rentalmain.rentalmain.model.FinancialCategory; // Use this everywhere
import com.xpro.rentalmain.rentalmain.model.FeatureKey;
import com.xpro.rentalmain.rentalmain.model.PaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RiskFeatureProcessor {

    private final FinancialRecordService recordService;

    /**
     * The Master Logic: Turns raw history into a structured Feature DTO.
     */
    public BehavioralFeatureRequestDTO engineerFeatures(UUID tenantId) {
        log.info("Processing financial data for tenant: {}", tenantId);

        // 1. Fetch raw data
        List<FinancialRecordResponse> history = recordService.getTenantHistory(tenantId);

        // 2. Map to hold our calculated percentages
        Map<FeatureKey, BigDecimal> features = new EnumMap<>(FeatureKey.class);

        // 3. Perform calculations using the Unified FinancialCategory Enum
        features.put(FeatureKey.RENT_CONSISTENCY, calculate(history, FinancialCategory.RENT));
        features.put(FeatureKey.UTILITY_PAYMENTS, calculate(history, FinancialCategory.UTILITY));
        features.put(FeatureKey.LOAN_REPAYMENT, calculate(history, FinancialCategory.LOAN));
        features.put(FeatureKey.AIRTIME_USAGE, calculate(history, FinancialCategory.AIRTIME));
        features.put(FeatureKey.SAVINGS_CONSISTENCY, calculate(history, FinancialCategory.SAVINGS));

        // 4. Behavior-based metrics
        features.put(FeatureKey.MOMO_VOLUME, calculateVolumeMetric(history));
        features.put(FeatureKey.TX_DIVERSITY, calculateDiversityMetric(history));

        // 5. Return the DTO
        // Note: Length of Residence is set to null as it usually comes from Tenant profile
        return new BehavioralFeatureRequestDTO(
                features.get(FeatureKey.RENT_CONSISTENCY),
                features.get(FeatureKey.UTILITY_PAYMENTS),
                features.get(FeatureKey.AIRTIME_USAGE),
                features.get(FeatureKey.SAVINGS_CONSISTENCY),
                features.get(FeatureKey.LOAN_REPAYMENT),
                features.get(FeatureKey.MOMO_VOLUME),
                features.get(FeatureKey.TX_DIVERSITY),
                null, // lengthOfResidence
                "Auto-Snapshot-" + LocalDate.now()
        );
    }

    /**
     * Generic calculator: (Successful Payments / Total Payments)
     * Returns null if no records found to allow the Math Engine to re-weight.
     */
    private BigDecimal calculate(List<FinancialRecordResponse> history, FinancialCategory category) {
        List<FinancialRecordResponse> categoryRecords = history.stream()
                .filter(r -> r.category() == category) // Works now! Both are FinancialCategory
                .toList();

        if (categoryRecords.isEmpty()) {
            log.debug("No records found for category: {}", category);
            return null;
        }

        long successCount = categoryRecords.stream()
                .filter(this::isPositiveBehavior)
                .count();

        return BigDecimal.valueOf(successCount)
                .divide(BigDecimal.valueOf(categoryRecords.size()), 4, RoundingMode.HALF_UP);
    }

    /**
     * Determines if a payment status counts as "Good Behavior"
     */
    private boolean isPositiveBehavior(FinancialRecordResponse record) {
        return record.status() == PaymentStatus.ON_TIME ||
                record.status() == PaymentStatus.GRACE_PERIOD;
    }

    private BigDecimal calculateVolumeMetric(List<FinancialRecordResponse> history) {
        if (history.isEmpty()) return null;
        // Logic: > 10 total verified transactions = high volume performance
        return (history.size() >= 10) ? BigDecimal.ONE : new BigDecimal("0.5");
    }

    private BigDecimal calculateDiversityMetric(List<FinancialRecordResponse> history) {
        if (history.isEmpty()) return null;
        long uniqueCategories = history.stream()
                .map(FinancialRecordResponse::category)
                .distinct()
                .count();
        // Normalize: 4+ different categories paid = 100% diversity
        return BigDecimal.valueOf(uniqueCategories)
                .divide(new BigDecimal("4.0"), 2, RoundingMode.HALF_UP)
                .min(BigDecimal.ONE); // Cap at 1.0
    }
}