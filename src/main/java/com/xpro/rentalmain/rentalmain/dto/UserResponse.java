package com.xpro.rentalmain.rentalmain.dto;


import com.xpro.rentalmain.rentalmain.model.Gender;
import com.xpro.rentalmain.rentalmain.model.UserStatus;
import com.xpro.rentalmain.rentalmain.model.UserType;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String username,
        String email,
        String firstName,
        String lastName,
        String phoneNumber,
        UserType role,
        Gender gender,
        UserStatus status
) {}