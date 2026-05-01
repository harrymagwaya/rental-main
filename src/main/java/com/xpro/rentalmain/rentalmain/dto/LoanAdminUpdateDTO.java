package com.xpro.rentalmain.rentalmain.dto;

import com.xpro.rentalmain.rentalmain.model.AuthorityLevel;

public record LoanAdminUpdateDTO(
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        String employeeId,
        String department,
        AuthorityLevel authorityLevel,
        AddressUpdateDTO officeAddress
) {}