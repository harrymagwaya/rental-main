package com.xpro.rentalmain.rentalmain.dto;

import com.xpro.rentalmain.rentalmain.model.Gender;

import java.time.LocalDate;
import java.util.UUID;

public record LandlordUpdateDTO(
        String firstName,
        String lastName,
        String middleName,
        String email,
        String profilePhoto,
        LocalDate dateOfBirth,
        Gender gender,
        AddressUpdateDTO homeAddress,
        AddressUpdateDTO workAddress
) {}