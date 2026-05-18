package com.xpro.rentalmain.rentalmain.dto;

import com.xpro.rentalmain.rentalmain.model.UserType;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;


@Builder
public class AuthResponse {

    @Getter
    private final String token;
    @Getter
    private final UUID userId;
    @Getter
    private final long expiresIn;
    @Getter
    private final UserType role;


    public AuthResponse(String token, UUID userId,long exp , UserType role) {
        this.token = token;
        this.userId = userId;
        this.expiresIn = exp;
        this.role = role;
    }
}

