package com.xpro.rentalmain.rentalmain.dto;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * A clean data carrier for transferring snapshots between the
 * Controller and the Service.
 */
public record BehavioralFeatureDTO(
        UUID id,                 // Nullable during creation
        String featureKey,       // e.g., "RENTAL_V1"
        Map<String, BigDecimal> featureValues
) {}