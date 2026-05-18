package com.xpro.rentalmain.rentalmain.entity;

import com.xpro.rentalmain.rentalmain.model.Auditable;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "tenant_capacities")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TenantCapacity extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID tenantId;

    // Monthly Averages in UGX
    private BigDecimal monthlyIncome;
    private BigDecimal avgMomoVolume;
    private BigDecimal avgUtilitySpend;
    private BigDecimal avgSavingsDeposit;
    private BigDecimal avgAirtimeSpend;

    private boolean isVerified; // Flag for manual verification
}