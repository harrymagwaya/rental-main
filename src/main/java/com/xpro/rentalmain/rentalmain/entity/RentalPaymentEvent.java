package com.xpro.rentalmain.rentalmain.entity;

import com.xpro.rentalmain.rentalmain.model.Auditable;
import com.xpro.rentalmain.rentalmain.model.PaymentMethod;
import com.xpro.rentalmain.rentalmain.model.PaymentStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "rental_payment_events",
        indexes = {
                @Index(name = "idx_rental_profile_id", columnList = "rental_profile_id"),
                @Index(name = "idx_payment_date", columnList = "payment_date")
        })
public class RentalPaymentEvent extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    // 🔗 Link to the lease (NOT just tenant)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rental_profile_id", nullable = false)
    private RentalProfile rentalProfile;

    // 📅 Payment timing
    private LocalDateTime dueDate;
    private LocalDateTime paymentDate;

    // 💰 Financials
    private BigDecimal expectedAmount;
    private BigDecimal amountPaid;

    // 📊 Derived behavior
    private Integer daysLate;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    private PaymentMethod method;

    private String transactionReference;



}