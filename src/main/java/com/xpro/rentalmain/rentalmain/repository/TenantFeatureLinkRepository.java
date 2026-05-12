package com.xpro.rentalmain.rentalmain.repository;

import com.xpro.rentalmain.rentalmain.entity.TenantFeatureLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenantFeatureLinkRepository extends JpaRepository<TenantFeatureLink, UUID> {

    Optional<TenantFeatureLink> findByTenantIdAndIsActiveTrue(UUID tenantId);

    boolean existsByFeatureSnapshotId(UUID tenantId);

    /**
     * Finds all unique Tenant IDs that have an active feature link.
     * This is the "Master List" for your batch scoring.
     */
    @Query("SELECT DISTINCT f.tenantId FROM TenantFeatureLink f WHERE f.isActive = true")
    List<UUID> findAllActiveTenantIds();
}
