package com.xpro.rentalmain.rentalmain.dto;

import com.xpro.rentalmain.rentalmain.entity.Address;

public record PropertyUpdateRequest(String name, String location, Integer numberOfUnits, AddressUpdateDTO address) {}