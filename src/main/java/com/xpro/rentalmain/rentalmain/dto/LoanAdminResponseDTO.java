package com.xpro.rentalmain.rentalmain.dto;

import com.xpro.rentalmain.rentalmain.model.AuthorityLevel;

import java.time.LocalDateTime;
import java.util.UUID;

public record LoanAdminResponseDTO(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        String employeeId,
        String department,
        AuthorityLevel authorityLevel,
        Integer totalApprovals,
        Integer totalRejections,
        AddressResponse officeAddress,
        LocalDateTime createdAt
) {}