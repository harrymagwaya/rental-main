package com.xpro.rentalmain.rentalmain.entity;

import com.xpro.rentalmain.rentalmain.model.RiskBand;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "credit_scores")
public class CreditScore {

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