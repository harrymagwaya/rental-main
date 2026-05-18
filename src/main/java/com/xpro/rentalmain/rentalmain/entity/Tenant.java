package com.xpro.rentalmain.rentalmain.entity;

import com.xpro.rentalmain.rentalmain.model.Auditable;
import com.xpro.rentalmain.rentalmain.model.TenantStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;


@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tenants")
public class Tenant extends Auditable {

    @Id
    private UUID id;

    private String firstName;
    private String lastName;

    @Column(unique = true, nullable = false)
    private String phoneNumber;

    @Column(unique = true)
    private String email;

    private String nationalId; // optional (Uganda NIN)


    @Enumerated(EnumType.STRING)
    private TenantStatus status;


    // 🔗 Relationships
    @OneToMany(mappedBy = "tenant", cascade = CascadeType.ALL)
    private java.util.List<RentalProfile> rentalProfiles;

    // Inside Tenant.java
    @ManyToOne
    @JoinColumn(name = "permanent_address_id")
    private Address permanentAddress;
    // getters & setters
}