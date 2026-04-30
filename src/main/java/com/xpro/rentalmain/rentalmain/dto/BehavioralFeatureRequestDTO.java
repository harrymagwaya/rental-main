package com.xpro.rentalmain.rentalmain.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * DTO for the incoming request to create a new behavioral snapshot.
 * Includes validation to ensure data stays within the 0.0 - 1.0 range.
 */
public record BehavioralFeatureRequestDTO(

        @NotNull(message = "Rent consistency is required")
        @DecimalMin("0.0") @DecimalMax("1.0")
        BigDecimal rentConsistency,

        @NotNull(message = "Utility payments is required")
        @DecimalMin("0.0") @DecimalMax("1.0")
        BigDecimal utilityPayments,

        @NotNull(message = "Airtime usage is required")
        @DecimalMin("0.0") @DecimalMax("1.0")
        BigDecimal airtimeUsage,

        @NotNull(message = "Savings consistency is required")
        @DecimalMin("0.0") @DecimalMax("1.0")
        BigDecimal savingsConsistency,

        @NotNull(message = "Loan repayment rate is required")
        @DecimalMin("0.0") @DecimalMax("1.0")
        BigDecimal loanRepaymentRate,

        @NotNull(message = "Mobile money volume is required")
        @DecimalMin("0.0") @DecimalMax("1.0")
        BigDecimal mobileMoneyVolume,

        @NotNull(message = "Transaction diversity is required")
        @DecimalMin("0.0") @DecimalMax("1.0")
        BigDecimal transactionDiversity,

        @NotNull(message = "Length of residence is required")
        @DecimalMin("0.0") @DecimalMax("1.0")
        BigDecimal lengthOfResidence,

        String snapshotVersion // e.g., "APR-2026-BETA"
) {}