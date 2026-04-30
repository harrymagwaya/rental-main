package com.xpro.rentalmain.rentalmain.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;


@SuperBuilder
@Data
@Entity
@Table(name = "rental_profiles",
        indexes = {
                @Index(name = "idx_tenant_id", columnList = "tenant_id"),
                @Index(name = "idx_unit_id", columnList = "unit_id")
        })
public class RentalProfile {

    @Id
    @GeneratedValue
    private UUID id;

    // 🔗 Tenant (who)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    // 🔗 Unit (where exactly)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id", nullable = false)
    private PropertyUnit unit;

    // 💰 Financial agreement
    private BigDecimal agreedRentAmount;
    private Integer rentDueDay; // e.g. 1 = first of month

    // 📅 Lease lifecycle
    private LocalDate leaseStartDate;
    private LocalDate leaseEndDate;

    @Enumerated(EnumType.STRING)
    private LeaseStatus status;

    // 📊 Optional behavioral shortcuts (can be computed instead)
    private Integer totalLatePayments;
    private Integer totalPayments;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum LeaseStatus {
        ACTIVE,
        TERMINATED,
        DEFAULTED
    }

    // getters & setters
}