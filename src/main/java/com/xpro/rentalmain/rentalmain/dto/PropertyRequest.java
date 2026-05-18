package com.xpro.rentalmain.rentalmain.dto;

import com.xpro.rentalmain.rentalmain.entity.Address;

public record PropertyRequest(String name, String location, Integer numberOfUnits, AddressRequest address) {}