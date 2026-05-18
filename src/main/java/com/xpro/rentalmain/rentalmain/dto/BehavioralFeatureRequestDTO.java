package com.xpro.rentalmain.rentalmain.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * DTO for the incoming request to create a new behavioral snapshot.
 * Includes validation to ensure data stays within the 0.0 - 1.0 range.
 */public record BehavioralFeatureRequestDTO(

        // Removed @NotNull to allow for missing data re-weighting
        @DecimalMin("0.0") @DecimalMax("1.0")
        BigDecimal rentConsistency,

        @DecimalMin("0.0") @DecimalMax("1.0")
        BigDecimal utilityPayments,

        @DecimalMin("0.0") @DecimalMax("1.0")
        BigDecimal airtimeUsage,

        @DecimalMin("0.0") @DecimalMax("1.0")
        BigDecimal savingsConsistency,

        @DecimalMin("0.0") @DecimalMax("1.0")
        BigDecimal loanRepaymentRate,

        @DecimalMin("0.0") @DecimalMax("1.0")
        BigDecimal mobileMoneyVolume,

        @DecimalMin("0.0") @DecimalMax("1.0")
        BigDecimal transactionDiversity,

        @DecimalMin("0.0") @DecimalMax("1.0")
        BigDecimal lengthOfResidence,

        String snapshotVersion
) {}