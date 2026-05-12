package com.xpro.rentalmain.rentalmain.entity;

import com.xpro.rentalmain.rentalmain.model.Auditable;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "tenant_feature_links")
public class TenantFeatureLink extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId; // Just the ID

    @Column(name = "feature_snapshot_id", nullable = false)
    private UUID featureSnapshotId; // Just the ID

    @Column(name = "is_active")
    private boolean isActive = true;
}