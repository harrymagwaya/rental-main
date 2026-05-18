package com.xpro.rentalmain.rentalmain.dto;

import lombok.Data;

@Data
public class CreateUserRequest {
    private String username;
    private String email;
    private String password;
    private String phoneNumber;
}