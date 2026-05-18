package com.xpro.rentalmain.rentalmain.repository;

import com.xpro.rentalmain.rentalmain.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, UUID> {

    // Use JPQL to select IDs where status matches the Enum value
    @Query("SELECT t.id FROM Tenant t WHERE t.status = com.xpro.rentalmain.rentalmain.model.TenantStatus.ACTIVE")
    List<UUID> findAllActiveTenantIds();
}
