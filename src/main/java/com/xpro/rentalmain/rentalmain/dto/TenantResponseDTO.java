package com.xpro.rentalmain.rentalmain.dto;

import com.xpro.rentalmain.rentalmain.entity.Tenant;

import java.time.LocalDateTime;
import java.util.UUID;

public record TenantResponseDTO(
        UUID id,
        String firstName,
        String lastName,
        String phoneNumber,
        String email,
        String nationalId,
        Tenant.TenantStatus status,
        LocalDateTime createdAt
) {}