package com.xpro.rentalmain.rentalmain.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tenants")
public class Tenant {

    @Id
    private UUID id;

    private String firstName;
    private String lastName;

    @Column(unique = true, nullable = false)
    private String phoneNumber;

    @Column(unique = true)
    private String email;

    private String nationalId; // optional (Uganda NIN)

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    private TenantStatus status;

    public enum TenantStatus {
        ACTIVE,
        INACTIVE,
        BLACKLISTED
    }

    // 🔗 Relationships
    @OneToMany(mappedBy = "tenant", cascade = CascadeType.ALL)
    private java.util.List<RentalProfile> rentalProfiles;

    // getters & setters
}