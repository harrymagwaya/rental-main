package com.xpro.rentalmain.rentalmain.dto;

import com.xpro.rentalmain.rentalmain.model.TenantStatus;

import java.util.UUID;

public record TenantUpdateDTO(
        String firstName,
        String lastName,
        String phoneNumber,
        String email,
        String nationalId,
        UUID permanentAddressId,
        TenantStatus status,
        AddressUpdateDTO address
) {}