package com.xpro.rentalmain.rentalmain.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Data Transfer Object for Behavioral Feature Snapshots.
 * Used for creating new snapshots and returning snapshot data to the UI.
 */
public record BehavioralFeatureDTO(
        UUID id,                          // Nullable when creating new snapshots

        // Core Rental & Utilities
        BigDecimal rentConsistency,       // 0.0 to 1.0 scale
        BigDecimal utilityPayments,       // 0.0 to 1.0 scale
        BigDecimal airtimeUsage,          // 0.0 to 1.0 scale

        // Financial Behavior
        BigDecimal savingsConsistency,    // 0.0 to 1.0 scale
        BigDecimal loanRepaymentRate,     // 0.0 to 1.0 scale
        BigDecimal mobileMoneyVolume,     // 0.0 to 1.0 scale

        // Stability & Diversity
        BigDecimal transactionDiversity,   // 0.0 to 1.0 scale
        BigDecimal lengthOfResidence      // 0.0 to 1.0 scale

) {}