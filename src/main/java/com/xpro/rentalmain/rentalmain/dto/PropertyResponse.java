package com.xpro.rentalmain.rentalmain.dto;

import java.util.List;
import java.util.UUID;

public record PropertyResponse(UUID id, String name, String location, int numberOfUnits, List<PropertyUnitResponse> units) {}