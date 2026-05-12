package com.xpro.rentalmain.rentalmain.repository;

import com.xpro.rentalmain.rentalmain.entity.RentalPaymentEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface RentalPaymentEventRepository extends JpaRepository<RentalPaymentEvent, UUID> {
    // Finds events by navigating: Event -> Profile -> Tenant ID
    List<RentalPaymentEvent> findByRentalProfileTenantIdOrderByPaymentDateDesc(UUID tenantId);

    @Query("SELECT e FROM RentalPaymentEvent e " +
            "WHERE e.rentalProfile.unit.property.id = :propertyId " +
            "ORDER BY e.paymentDate DESC")
    List<RentalPaymentEvent> findAllByPropertyId(@Param("propertyId") UUID propertyId);
}
