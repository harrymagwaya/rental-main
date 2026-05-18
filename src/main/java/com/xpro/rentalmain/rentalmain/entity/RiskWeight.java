package com.xpro.rentalmain.rentalmain.entity;

import com.xpro.rentalmain.rentalmain.model.Auditable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "risk_model_weights")
public class RiskWeight extends Auditable { // Using your standard Auditable base class

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "feature_key", updatable = false, nullable = false, unique = true)
    private String featureKey; // e.g., "RENT", "PAYMENT", "UTILITY"

    @Column(name = "weight_value", precision = 5, scale = 4, nullable = false)
    private BigDecimal weightValue;

    @Column(name = "is_active")
    private boolean isActive = true;

}