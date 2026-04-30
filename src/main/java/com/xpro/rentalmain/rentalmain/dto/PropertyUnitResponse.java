package com.xpro.rentalmain.rentalmain.dto;

import com.xpro.rentalmain.rentalmain.model.UnitStatus;

import java.util.UUID;

public record PropertyUnitResponse(UUID id, String unitNumber, java.math.BigDecimal rentAmount, UnitStatus status, UUID tenantId) {}