package com.xpro.rentalmain.rentalmain.dto;

import com.xpro.rentalmain.rentalmain.model.RiskBand;
import com.xpro.rentalmain.rentalmain.model.RiskCategory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record EligibilityResponseDTO(
        UUID tenantId,

        // Risk Section
        Integer creditScore,
        RiskBand riskBand,
        RiskCategory riskCategory,

        // Capacity Section (The "How Much")
        BigDecimal monthlyIncome,
        BigDecimal totalEconomicFootprint,

        // Control/Limit Section
        BigDecimal minEligibleLimit,
        BigDecimal maxEligibleLimit,
        boolean isCalculationAllowed,

        // Metadata
        String statusMessage,
        LocalDateTime updatedAt
) {}