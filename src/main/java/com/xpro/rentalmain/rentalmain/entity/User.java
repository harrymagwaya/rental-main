package com.xpro.rentalmain.rentalmain.entity;

import com.xpro.rentalmain.rentalmain.model.Auditable;
import com.xpro.rentalmain.rentalmain.model.Gender;
import com.xpro.rentalmain.rentalmain.model.UserStatus;
import com.xpro.rentalmain.rentalmain.model.UserType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@SuperBuilder
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false)
@Entity
@Table(name = "users")
public class User extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String username; // For login (can be a handle)

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password; // Bcrypt encoded hash

    private String firstName;
    private String lastName;

    @Column(length = 15)
    private String phoneNumber; // Critical for emergency SMS/Calls

    @Enumerated(EnumType.STRING)
    private UserType role;

    @Enumerated(EnumType.STRING)
    private UserStatus userStatus;

    private Gender gender;

}