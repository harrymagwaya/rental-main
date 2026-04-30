package com.xpro.rentalmain.rentalmain.service;

import com.xpro.rentalmain.rentalmain.dto.*;
import com.xpro.rentalmain.rentalmain.entity.Property;
import com.xpro.rentalmain.rentalmain.entity.PropertyUnit;
import com.xpro.rentalmain.rentalmain.model.UnitStatus;
import com.xpro.rentalmain.rentalmain.repository.PropertyRepository;
import com.xpro.rentalmain.rentalmain.repository.PropertyUnitRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PropertyService {

    private final PropertyRepository propertyRepo;
    private final PropertyUnitRepository unitRepo;

    // --- PROPERTY METHODS ---

    @Transactional
    public PropertyResponse createProperty(PropertyRequest request) {
        log.info("Creating new property: {} with {} units capacity", request.name(), request.numberOfUnits());
        Property property = Property.builder()
                .name(request.name())
                .location(request.location())
                .numberOfUnits(request.numberOfUnits())
                .createdAt(LocalDateTime.now())
                .build();
        return mapToPropertyResponse(propertyRepo.save(property));
    }

    @Transactional(readOnly = true)
    public PropertyResponse getPropertyById(UUID id) {
        log.info("Retrieving property ID: {}", id);
        return propertyRepo.findById(id)
                .map(this::mapToPropertyResponse)
                .orElseThrow(() -> new EntityNotFoundException("Property not found with ID: " + id));
    }

    @Transactional
    public PropertyResponse updateProperty(UUID id, PropertyUpdateRequest request) {
        log.info("Updating property ID: {}", id);
        Property property = propertyRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Property not found"));

        // Null-safe checks
        if (request.name() != null) property.setName(request.name());
        if (request.location() != null) property.setLocation(request.location());

        if (request.numberOfUnits() != null) {
            long existingUnits = unitRepo.countByPropertyId(id);
            if (request.numberOfUnits() < existingUnits) {
                throw new IllegalArgumentException("New capacity (" + request.numberOfUnits() +
                        ") cannot be less than the " + existingUnits + " units already created.");
            }
            property.setNumberOfUnits(request.numberOfUnits());
        }

        return mapToPropertyResponse(propertyRepo.save(property));
    }

    @Transactional
    public void deleteProperty(UUID id) {
        log.warn("Attempting to delete property ID: {}", id);

        if (unitRepo.existsByPropertyIdAndStatus(id, UnitStatus.OCCUPIED)) {
            log.error("Delete failed: Property {} has occupied units.", id);
            throw new IllegalStateException("Cannot delete a property with active tenants.");
        }

        propertyRepo.deleteById(id);
        log.info("Property {} and its vacant units deleted successfully.", id);
    }

    // --- UNIT METHODS ---

    @Transactional
    public PropertyUnitResponse addSingleUnit(UUID propertyId, PropertyUnitRequest request) {
        log.info("Adding unit {} to property {}", request.unitNumber(), propertyId);

        Property property = propertyRepo.findById(propertyId)
                .orElseThrow(() -> new EntityNotFoundException("Property not found"));

        // 1. Capacity Check
        long currentUnitCount = unitRepo.countByPropertyId(propertyId);
        if (currentUnitCount >= property.getNumberOfUnits()) {
            throw new IllegalStateException("Max capacity of " + property.getNumberOfUnits() + " units reached.");
        }

        // 2. Duplicate Number Check (Within this property)
        if (unitRepo.existsByPropertyIdAndUnitNumber(propertyId, request.unitNumber())) {
            throw new IllegalArgumentException("Unit number " + request.unitNumber() + " already exists in this property.");
        }

        PropertyUnit unit = PropertyUnit.builder()
                .property(property)
                .unitNumber(request.unitNumber())
                .rentAmount(request.rentAmount())
                .status(UnitStatus.VACANT)
                .build();

        return mapToUnitResponse(unitRepo.save(unit));
    }

    @Transactional(readOnly = true)
    public PropertyUnitResponse getUnitById(UUID id) {
        return unitRepo.findById(id)
                .map(this::mapToUnitResponse)
                .orElseThrow(() -> new EntityNotFoundException("Unit not found with ID: " + id));
    }

    @Transactional
    public PropertyUnitResponse updateUnit(UUID id, PropertyUnitUpdateRequest request) {
        log.info("Updating unit ID: {}", id);
        PropertyUnit unit = unitRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Unit not found"));

        // Null-safe partial updates
        if (request.unitNumber() != null) unit.setUnitNumber(request.unitNumber());
        if (request.rentAmount() != null) unit.setRentAmount(request.rentAmount());
        if (request.status() != null) unit.setStatus(request.status());

        return mapToUnitResponse(unitRepo.save(unit));
    }

    @Transactional
    public void deleteUnit(UUID id) {
        PropertyUnit unit = unitRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Unit not found"));

        if (unit.getStatus() == UnitStatus.OCCUPIED) {
            log.error("Delete failed: Unit {} is occupied.", id);
            throw new IllegalStateException("Cannot delete an occupied unit.");
        }

        unitRepo.delete(unit);
        log.info("Unit {} deleted successfully.", id);
    }

    /**
     * GET BY LANDLORD: For the Landlord's "My Properties" Dashboard
     */
    @Transactional(readOnly = true)
    public List<PropertyResponse> getPropertiesByLandlord(UUID landlordId) {
        log.info("Fetching all properties for landlord: {}", landlordId);
        return propertyRepo.findByLandlordId(landlordId).stream()
                .map(this::mapToPropertyResponse)
                .toList();
    }

    /**
     * GET BY PROPERTY: For the "Building Details" view (list all units)
     */
    @Transactional(readOnly = true)
    public List<PropertyUnitResponse> getUnitsByProperty(UUID propertyId) {
        log.info("Fetching all units for property: {}", propertyId);
        return unitRepo.findByPropertyId(propertyId).stream()
                .map(this::mapToUnitResponse)
                .toList();
    }

    /**
     * GET BY TENANT: For the Tenant's "My Home" view
     * Essential for the Scoring Engine to find the rentAmount.
     */
    @Transactional(readOnly = true)
    public PropertyUnitResponse getUnitByTenant(UUID tenantId) {
        log.info("Fetching assigned unit for tenant: {}", tenantId);
        return unitRepo.findByTenantId(tenantId)
                .map(this::mapToUnitResponse)
                .orElseThrow(() -> new EntityNotFoundException("No unit assigned to tenant: " + tenantId));
    }

    // --- MAPPERS ---

    private PropertyUnitResponse mapToUnitResponse(PropertyUnit unit) {
        UUID tenantId = (unit.getRentalProfile() != null) ? unit.getRentalProfile().getId() : null;
        return new PropertyUnitResponse(
                unit.getId(),
                unit.getUnitNumber(),
                unit.getRentAmount(),
                unit.getStatus(),
                tenantId
        );
    }

    private PropertyResponse mapToPropertyResponse(Property p) {
        return new PropertyResponse(
                p.getId(),
                p.getName(),
                p.getLocation(),
                p.getNumberOfUnits(),
                p.getUnits() != null ? p.getUnits().stream().map(this::mapToUnitResponse).toList() : List.of()
        );
    }
}