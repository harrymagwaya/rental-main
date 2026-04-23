package com.xpro.rentalmain.rentalmain.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "behavioral_features")
public class BehavioralFeatures {

    @Id
    @GeneratedValue
    private UUID id;

    private UUID tenantId;

    private BigDecimal rentConsistencyScore;
    private BigDecimal avgDaysLate;
    private BigDecimal latePaymentFrequency;
    private BigDecimal paymentVariance;

    private Integer tenancyDurationMonths;

    private Integer negativeEventCount;
    private BigDecimal avgResolutionTime;

    private BigDecimal spendingVelocity;
    private BigDecimal liquidityBufferRatio;

    private LocalDateTime featureGeneratedAt;

    // getters & setters
}