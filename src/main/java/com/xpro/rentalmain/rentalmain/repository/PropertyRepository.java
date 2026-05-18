package com.xpro.rentalmain.rentalmain.repository;

import com.xpro.rentalmain.rentalmain.entity.Property;
import com.xpro.rentalmain.rentalmain.entity.User;
import com.xpro.rentalmain.rentalmain.model.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PropertyRepository extends JpaRepository<Property, UUID> {
    // Finds all buildings owned by a specific landlord
    List<Property> findByLandlordId(UUID landlordId);

}