package com.xpro.rentalmain.rentalmain.repository;

import com.xpro.rentalmain.rentalmain.entity.RentalProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RentalProfileRepository extends JpaRepository<RentalProfile, UUID> {

    List<RentalProfile> findByTenantId(UUID tenantId);

    /**
     * Finds the active lease by checking the status enum
     * and traversing the Tenant object's ID.
     */
    @Query("SELECT rp FROM RentalProfile rp WHERE rp.tenant.id = :tenantId AND rp.status = 'ACTIVE'")
    Optional<RentalProfile> findActiveByTenantId(@Param("tenantId") UUID tenantId);
}
