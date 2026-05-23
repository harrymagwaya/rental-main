package com.xpro.rentalmain.rentalmain.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;



import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TenantFeatureHistoryDTO(
        UUID linkId,
        UUID tenantId,
        UUID snapshotId,
        boolean active,

        // Core metrics for frontend dashboard visualization
        BigDecimal rentConsistency,
        BigDecimal utilityPayments,
        BigDecimal airtimeUsage,
        BigDecimal savingsConsistency,
        BigDecimal loanRepaymentRate,
        BigDecimal mobileMoneyVolume,
        BigDecimal transactionDiversity,
        BigDecimal lengthOfResidence,

        LocalDateTime linkedAt
) {}

