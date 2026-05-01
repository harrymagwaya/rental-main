package com.xpro.rentalmain.rentalmain.entity;

import com.xpro.rentalmain.rentalmain.model.Auditable;
import com.xpro.rentalmain.rentalmain.model.EventType;
import com.xpro.rentalmain.rentalmain.model.Severity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "negative_events")
public class NegativeEvent extends Auditable { // resilience event

    @Id
    private UUID eventId;

    private UUID tenantId;

    @Enumerated(EnumType.STRING)
    private EventType eventType;

    private LocalDateTime eventTimestamp;
    private LocalDateTime resolutionTimestamp;

    private Integer resolutionTimeHours;

    @Enumerated(EnumType.STRING)
    private Severity severity;

}