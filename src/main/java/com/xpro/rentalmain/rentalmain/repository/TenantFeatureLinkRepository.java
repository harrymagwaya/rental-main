package com.xpro.rentalmain.rentalmain.repository;

import com.xpro.rentalmain.rentalmain.entity.TenantFeatureLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenantFeatureLinkRepository extends JpaRepository<TenantFeatureLink, UUID> {

    Optional<TenantFeatureLink> findByTenantIdAndIsActiveTrue(UUID tenantId);
}
