package com.xpro.rentalmain.rentalmain.entity;

import com.xpro.rentalmain.rentalmain.model.Auditable;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "behavioral_features")
@Data
public class BehavioralFeatures extends Auditable {

    @Id
    @GeneratedValue
    private UUID id;

    private String featureKey;    // RENTAL_V1

    @ElementCollection
    @CollectionTable(
            name = "tenant_feature_data",
            joinColumns = @JoinColumn(name = "feature_snapshot_id")
    )
    @MapKeyColumn(name = "feature_key")
    @Column(name = "feature_value")
    private Map<String, BigDecimal> featureValues = new HashMap<>();
}