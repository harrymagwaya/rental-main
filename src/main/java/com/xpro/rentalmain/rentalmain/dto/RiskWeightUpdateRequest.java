package com.xpro.rentalmain.rentalmain.dto;

import java.math.BigDecimal;
import java.util.UUID;

// For updating an existing record
public record RiskWeightUpdateRequest(
        BigDecimal weightValue,
        boolean active
) {}