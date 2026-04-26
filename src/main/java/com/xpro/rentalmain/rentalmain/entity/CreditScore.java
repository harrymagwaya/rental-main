package com.xpro.rentalmain.rentalmain.entity;

import com.xpro.rentalmain.rentalmain.model.Auditable;
import com.xpro.rentalmain.rentalmain.model.RiskBand;
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

    private BigDecimal probabilityOfDefault;

    @Enumerated(EnumType.STRING)
    private RiskBand riskBand;

    private Integer score;

    private String modelVersion;

    private LocalDateTime scoredAt;

}