package com.xpro.rentalmain.rentalmain.entity;


import com.xpro.rentalmain.rentalmain.model.UserStatus;
import com.xpro.rentalmain.rentalmain.model.UserType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
@AllArgsConstructor
@Builder
public class UserIdentity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    private String phoneNumber;

    @Column(nullable = false)
    private String password;

    private UserStatus userStatus;

    private UserType userType;

}
