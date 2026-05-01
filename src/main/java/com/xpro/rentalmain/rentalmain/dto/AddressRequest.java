package com.xpro.rentalmain.rentalmain.dto;

public record AddressRequest(
        String street,
        String city,
        String country,
        String zipCode,
        String postalCode
) {}