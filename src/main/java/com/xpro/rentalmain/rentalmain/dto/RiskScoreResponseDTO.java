package com.xpro.rentalmain.rentalmain.dto;

import com.xpro.rentalmain.rentalmain.model.RiskBand;
import com.xpro.rentalmain.rentalmain.model.RiskCategory;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * The final output returned to the user/dashboard after a credit calculation.
 */
public record RiskScoreResponseDTO(
        UUID tenantId,

        // The "Raw" Math Results
        BigDecimal probabilityOfDefault, // The 0.0 - 1.0 result from the model
        RiskCategory riskCategory,       // LOW_RISK, MEDIUM_RISK, HIGH_RISK

        // The Business Logic Results
        Integer creditScore,             // The 0-900 scaled value
        RiskBand riskBand,               // PLATINUM, GOLD, SILVER, BRONZE

        // Metadata
        String modelVersion,
        LocalDateTime calculatedAt
) {}