package com.xpro.rentalmain.rentalmain.repository;

import com.xpro.rentalmain.rentalmain.entity.Eligibility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EligibilityRepository extends JpaRepository<Eligibility, UUID> {
    Optional<Eligibility> findByTenantId(UUID tenantId);
}
