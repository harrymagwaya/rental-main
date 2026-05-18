package com.xpro.rentalmain.rentalmain.dto;

import com.xpro.rentalmain.rentalmain.entity.Address;
import com.xpro.rentalmain.rentalmain.entity.Tenant;
import com.xpro.rentalmain.rentalmain.model.TenantStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record TenantResponseDTO(
        UUID id,
        String firstName,
        String lastName,
        String phoneNumber,
        String email,
        String nationalId,
        TenantStatus status,
        LocalDateTime createdAt,
        AddressResponse  address
) {}