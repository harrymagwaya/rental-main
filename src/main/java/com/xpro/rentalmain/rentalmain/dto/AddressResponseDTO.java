package com.xpro.rentalmain.rentalmain.dto;

import java.util.UUID;

// A clean response DTO for the address fields
public record AddressResponseDTO(
        UUID id,
        String street,
        String city,
        String zipCode,
        String country,
        String postalCode
) {}