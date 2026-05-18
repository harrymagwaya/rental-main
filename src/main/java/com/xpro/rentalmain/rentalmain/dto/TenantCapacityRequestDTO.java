package com.xpro.rentalmain.rentalmain.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record TenantCapacityRequestDTO(
        UUID tenantId,
        BigDecimal monthlyIncome,
        BigDecimal avgMomoVolume,
        BigDecimal avgUtilitySpend,
        BigDecimal avgSavingsDeposit,
        BigDecimal avgAirtimeSpend,
        Boolean isVerified
) {}