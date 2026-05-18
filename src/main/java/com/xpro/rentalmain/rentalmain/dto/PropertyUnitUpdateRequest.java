package com.xpro.rentalmain.rentalmain.dto;

import com.xpro.rentalmain.rentalmain.model.UnitStatus;

public record PropertyUnitUpdateRequest(String unitNumber, java.math.BigDecimal rentAmount, UnitStatus status) {}