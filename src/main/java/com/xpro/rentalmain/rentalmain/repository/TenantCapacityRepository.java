package com.xpro.rentalmain.rentalmain.repository;

import com.xpro.rentalmain.rentalmain.entity.TenantCapacity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenantCapacityRepository extends JpaRepository<TenantCapacity, UUID> {
    Optional<TenantCapacity> findByTenantId(UUID tenantId);
}
