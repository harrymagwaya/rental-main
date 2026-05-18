package com.xpro.rentalmain.rentalmain.entity;

import com.xpro.rentalmain.rentalmain.model.Auditable;
import com.xpro.rentalmain.rentalmain.model.UnitStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.UUID;


@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "property_units",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"property_id", "unit_number"})
        })
public class PropertyUnit extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    private String unitNumber; // e.g. "A1", "Room 3", "B-204"

    private BigDecimal rentAmount;

    @Enumerated(EnumType.STRING)
    private UnitStatus status;

}