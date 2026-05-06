package com.xpro.rentalmain.rentalmain.repository;

import com.xpro.rentalmain.rentalmain.entity.RentalProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RentalProfileRepository extends JpaRepository<RentalProfile, UUID> {

    List<RentalProfile> findByTenantId(UUID tenantId);
}
