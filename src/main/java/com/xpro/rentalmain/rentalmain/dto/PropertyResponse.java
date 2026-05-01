package com.xpro.rentalmain.rentalmain.dto;

import com.xpro.rentalmain.rentalmain.entity.Address;

import java.util.List;
import java.util.UUID;

public record PropertyResponse(UUID id, String name, String location, int numberOfUnits, AddressResponseDTO address, List<PropertyUnitResponse> units) {}