package com.xpro.rentalmain.rentalmain.entity;

import com.xpro.rentalmain.rentalmain.model.Auditable;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "behavioral_features")
@Data
public class BehavioralFeatures extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    // --- CORE RENTAL ---
    @Column(precision = 5, scale = 4)
    private BigDecimal rentConsistency; // % of months paid on time

    // --- UTILITIES & BILLS ---
    @Column(precision = 5, scale = 4)
    private BigDecimal utilityPayments; // Consistency in Umeme/NWSC payments

    @Column(precision = 5, scale = 4)
    private BigDecimal airtimeUsage; // Average monthly spend vs. volatility

    // --- FINANCIAL DISCIPLINE ---
    @Column(precision = 5, scale = 4)
    private BigDecimal savingsConsistency; // Frequency of deposits into savings

    @Column(precision = 5, scale = 4)
    private BigDecimal loanRepaymentRate; // Performance on previous micro-loans

    @Column(precision = 5, scale = 4)
    private BigDecimal mobileMoneyVolume; // Total throughput (normalized)

    // --- STABILITY INDICATORS ---
    @Column(precision = 5, scale = 4)
    private BigDecimal transactionDiversity; // Do they pay different types of bills?

    @Column(precision = 5, scale = 4)
    private BigDecimal lengthOfResidence; // Stability in their current location

}