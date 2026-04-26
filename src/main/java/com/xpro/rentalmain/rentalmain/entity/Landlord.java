package com.xpro.rentalmain.rentalmain.entity;

import com.xpro.rentalmain.rentalmain.model.Gender;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;


@Data
@Entity
@AllArgsConstructor
@Builder
public class Landlord {

    @Id
    @Column(name = "user_id", updatable = false, nullable = false)
    private UUID id;

    @Column(unique = true)
    private String username;      // Unique identifier for login

    private String password;      // Hashed password (never store in plain text!)

    private String email;         // Contact/recovery info

    private String profilePhoto;


    private String firstName;
    private String lastName;
    private String middleName;
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private UUID profilePicture;

    @OneToOne
    @JoinColumn(name = "home_address_id", referencedColumnName = "id")
    private Address homeAddress;

    @OneToOne
    @JoinColumn(name = "work_address_id", referencedColumnName = "id")
    private Address workAddress;
}
