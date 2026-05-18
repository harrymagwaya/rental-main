package com.xpro.rentalmain.rentalmain.dto;

public record AddressUpdateDTO(
        String street,
        String city,
        String country,
        String zipCode,
        String postalCode
) {}