package com.xpro.rentalmain.rentalmain.entity;

import com.xpro.rentalmain.rentalmain.model.Auditable;
import com.xpro.rentalmain.rentalmain.model.RiskBand;
import com.xpro.rentalmain.rentalmain.model.RiskCategory; // <-- Added Import
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "credit_scores")
public class CreditScore  extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private UUID tenantId;

    @Column(precision = 5, scale = 4) // Good practice to enforce math precision here too!
    private BigDecimal probabilityOfDefault;

    @Enumerated(EnumType.STRING)
    private RiskCategory riskCategory; // <-- Added Field (From the Math Model)

    @Enumerated(EnumType.STRING)
    private RiskBand riskBand;         // (From the Business Logic)

    private Integer score;             // The 0-900 number

    private String modelVersion;

    private LocalDateTime scoredAt;

}