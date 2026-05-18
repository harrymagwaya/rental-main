package com.xpro.rentalmain.rentalmain.dto;

import java.util.UUID;

public record AddressResponse(
        UUID id,
        String street,
        String city,
        String country,
        String zipCode,
        String postalCode
) {}