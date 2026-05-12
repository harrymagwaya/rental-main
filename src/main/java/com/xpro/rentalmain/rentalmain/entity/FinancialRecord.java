package com.xpro.rentalmain.rentalmain.entity;

import com.xpro.rentalmain.rentalmain.model.Auditable;
import com.xpro.rentalmain.rentalmain.model.FinancialCategory; // Enum: RENT, MOMO, UTILITY, etc.
import com.xpro.rentalmain.rentalmain.model.PaymentStatus;    // Enum: PENDING, CONFIRMED
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "financial_records")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class FinancialRecord extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private UUID tenantId;

    @Column(unique = true) // Prevents duplicate MoMo/Bank refs
    private String txnId;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 50)
    private FinancialCategory category;

    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDateTime transactionDate;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private String referenceNote; // e.g., "April Yaka Bill"
}