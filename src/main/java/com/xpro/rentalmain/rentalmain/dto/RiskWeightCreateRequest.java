package com.xpro.rentalmain.rentalmain.dto;

import java.math.BigDecimal;
import java.util.UUID;

// For creating a brand new weight
public record RiskWeightCreateRequest(
        String featureKey,
        BigDecimal weightValue
) {}
