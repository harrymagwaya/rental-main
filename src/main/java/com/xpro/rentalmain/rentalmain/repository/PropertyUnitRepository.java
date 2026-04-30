package com.xpro.rentalmain.rentalmain.repository;

import com.xpro.rentalmain.rentalmain.entity.PropertyUnit;
import com.xpro.rentalmain.rentalmain.model.UnitStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PropertyUnitRepository extends JpaRepository<PropertyUnit, UUID> {

    /**
     * Used for Capacity Validation.
     */
    long countByPropertyId(UUID propertyId);

    /**
     * Used for Delete Protection.
     * Checks if any unit in a property is currently occupied.
     */
    boolean existsByPropertyIdAndStatus(UUID propertyId, UnitStatus status);

    /**
     * Used for Duplicate Prevention.
     * Ensures "Room 101" isn't created twice for the same building.
     */
    boolean existsByPropertyIdAndUnitNumber(UUID propertyId, String unitNumber);

    /**
     * Standard fetch for property dashboards.
     */
    List<PropertyUnit> findByPropertyId(UUID propertyId);

    // Finds the specific unit occupied by a tenant via the RentalProfile bridge
    @Query("SELECT u FROM PropertyUnit u JOIN u.rentalProfile rp WHERE rp.tenantId = :tenantId")
    Optional<PropertyUnit> findByTenantId(@Param("tenantId") UUID tenantId);
}