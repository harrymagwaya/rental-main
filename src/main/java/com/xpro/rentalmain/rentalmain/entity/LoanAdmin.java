package com.xpro.rentalmain.rentalmain.entity;

import com.xpro.rentalmain.rentalmain.model.AuthorityLevel;
import com.xpro.rentalmain.rentalmain.model.Gender;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "loan_admins")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoanAdmin {

    @Id
    @Column(name = "user_id")
    private UUID id;

    // Master Identity Mirroring
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;

    // Admin Specifics
    private String employeeId;     // For internal tracking
    private String department;     // e.g., "Micro-Credit", "Risk Assessment"

    @Enumerated(EnumType.STRING)
    private AuthorityLevel authorityLevel; // Determines max loan they can approve

    private Integer totalApprovals;
    private Integer totalRejections;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Inside LoanAdmin entity
    @OneToOne
    @JoinColumn(name = "office_address_id", referencedColumnName = "id")
    private Address officeAddress; // Where the admin is based
}