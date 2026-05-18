package com.xpro.rentalmain.rentalmain.entity;

import com.xpro.rentalmain.rentalmain.model.Auditable;
import com.xpro.rentalmain.rentalmain.model.RiskBand;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "eligibility")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Eligibility extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID tenantId;

    // Admin Controls
    private boolean isCalculationAllowed;
    private String adminNotes;
    private UUID lastActionedBy;

    // Persisted Limit Snapshot (The UGX Range)
    private BigDecimal currentMinLimit;
    private BigDecimal currentMaxLimit;

    @Enumerated(EnumType.STRING)
    private RiskBand lastCalculatedBand;

    private LocalDateTime lastReviewedAt;
}