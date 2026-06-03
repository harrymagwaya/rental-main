package com.xpro.rentalmain.rentalmain.service;

import com.xpro.rentalmain.rentalmain.dto.*;
import com.xpro.rentalmain.rentalmain.entity.*;
import com.xpro.rentalmain.rentalmain.model.UnitStatus;
import com.xpro.rentalmain.rentalmain.repository.PropertyRepository;
import com.xpro.rentalmain.rentalmain.repository.PropertyUnitRepository;
import com.xpro.rentalmain.rentalmain.repository.RentalProfileRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PropertyService {

    private final PropertyRepository propertyRepo;
    private final PropertyUnitRepository unitRepo;
    private final AddressService addressService;

    private final RentalProfileRepository rentalProfileRepo;

    private final LandlordService landlordService;

    @Transactional
    public PropertyResponse createProperty(PropertyRequest request) {
        Property property = Property.builder()
                .name(request.name())
                .location(request.location())
                .numberOfUnits(request.numberOfUnits() != null ? request.numberOfUnits() : 0)
                .createdAt(LocalDateTime.now())
                .build();

        if (request.address() != null) {
            AddressResponse savedAddr = addressService.createAddress(request.address());
            Address addressEntity = addressService.getAddressEntity(savedAddr.id());
            property.setAddress(addressEntity);
        }

        return mapToPropertyResponse(propertyRepo.save(property));
    }

    @Transactional(readOnly = true)
    public PropertyResponse getPropertyById(UUID id) {
        return propertyRepo.findById(id)
                .map(this::mapToPropertyResponse)
                .orElseThrow(() -> new EntityNotFoundException("Property not found with ID: " + id));
    }

    public Page<PropertyResponse> getAllProperties(Pageable pageable){
        return propertyRepo.findAll(pageable).map(this::mapToPropertyResponse);
    }

    /**
     * ATTACH LANDLORD: Links a Landlord entity to an existing Property
     */
    @Transactional
    public PropertyResponse attachLandlord(UUID propertyId, UUID landlordId) {
        Property property = propertyRepo.findById(propertyId)
                .orElseThrow(() -> new EntityNotFoundException("Property not found"));

        // We fetch the entity because the relationship requires the object, not just the ID
        Landlord landlord = landlordService.getLandlordEntity(landlordId);

        property.setLandlord(landlord);
        return mapToPropertyResponse(propertyRepo.save(property));
    }

    /**
     * GET BY LANDLORD ID: Standard query
     */
    @Transactional(readOnly = true)
    public List<PropertyResponse> getPropertiesByLandlord(UUID landlordId) {
        return propertyRepo.findByLandlordId(landlordId).stream()
                .map(this::mapToPropertyResponse)
                .toList();
    }

    @Transactional
    public PropertyResponse updateProperty(UUID id, PropertyUpdateRequest request) {
        Property property = propertyRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Property not found"));

        if (request.name() != null) property.setName(request.name());
        if (request.location() != null) property.setLocation(request.location());

        if (request.numberOfUnits() != null) {
            long existingUnits = unitRepo.countByPropertyId(id);
            if (request.numberOfUnits() < existingUnits) {
                throw new IllegalArgumentException("Capacity cannot be less than existing units.");
            }
            property.setNumberOfUnits(request.numberOfUnits());
        }

        if (request.address() != null) {
            if (property.getAddress() != null) {
                addressService.updateAddress(property.getAddress().getId(), request.address());
            } else {
                var addrUpdate = request.address();
                AddressRequest newRequest = new AddressRequest(
                        addrUpdate.street(), addrUpdate.city(), addrUpdate.country(),
                        addrUpdate.zipCode(), addrUpdate.postalCode()
                );
                AddressResponse newAddr = addressService.createAddress(newRequest);
                property.setAddress(addressService.getAddressEntity(newAddr.id()));
            }
        }

        return mapToPropertyResponse(propertyRepo.save(property));
    }

    @Transactional
    public void deleteProperty(UUID id) {
        if (unitRepo.existsByPropertyIdAndStatus(id, UnitStatus.OCCUPIED)) {
            throw new IllegalStateException("Cannot delete a property with active tenants.");
        }
        propertyRepo.deleteById(id);
    }

    // --- UNIT METHODS ---

    @Transactional
    public PropertyUnitResponse addSingleUnit(UUID propertyId, PropertyUnitRequest request) {
        Property property = propertyRepo.findById(propertyId)
                .orElseThrow(() -> new EntityNotFoundException("Property not found"));

        if (unitRepo.countByPropertyId(propertyId) >= property.getNumberOfUnits()) {
            throw new IllegalStateException("Max capacity reached.");
        }

        PropertyUnit unit = PropertyUnit.builder()
                .property(property)
                .unitNumber(request.unitNumber())
                .rentAmount(request.rentAmount())
                .status(UnitStatus.VACANT)
                .build();

        return mapToUnitResponse(unitRepo.saveAndFlush(unit));
    }

    /**
     * GET ALL UNITS FOR A SPECIFIC PROPERTY
     */
    @Transactional(readOnly = true)
    public List<PropertyUnitResponse> getUnitsByProperty(UUID propertyId) {
        log.info("Fetching all units for property: {}", propertyId);

        // Ensure the property exists first (optional but good for error feedback)
        if (!propertyRepo.existsById(propertyId)) {
            throw new EntityNotFoundException("Property not found with ID: " + propertyId);
        }

        return unitRepo.findByPropertyId(propertyId).stream()
                .map(this::mapToUnitResponse)
                .toList();
    }

    // Add these to PropertyService if they went missing:

    @Transactional
    public PropertyUnitResponse updateUnit(UUID id, PropertyUnitUpdateRequest request) {
        PropertyUnit unit = unitRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Unit not found"));

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
            throw new IllegalStateException("Cannot delete an occupied unit.");
        }
        unitRepo.delete(unit);
    }

    @Transactional(readOnly = true)
    public PropertyUnitResponse getUnitByTenant(UUID tenantId) {
        log.info("Fetching unit for tenant via Repository: {}", tenantId);

        // 1. Query the DB directly for the active profile
        RentalProfile profile = rentalProfileRepo.findByTenantId(tenantId)
                .stream()
                .filter(p -> p.getStatus() == RentalProfile.LeaseStatus.ACTIVE)
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("No active lease found for tenant: " + tenantId));

        // 2. Return the unit details
        return getUnitById(profile.getUnit().getId());
    }

    @Transactional(readOnly = true)
    public PropertyUnitResponse getUnitById(UUID id) {
        return unitRepo.findById(id)
                .map(this::mapToUnitResponse)
                .orElseThrow(() -> new EntityNotFoundException("Unit not found with ID: " + id));
    }

    // Inside PropertyService.java

    public PropertyUnit getUnitEntity(UUID unitId) {
        return unitRepo.findById(unitId)
                .orElseThrow(() -> new EntityNotFoundException("Unit not found"));
    }

    @Transactional
    public void updateUnitStatus(UUID unitId, UnitStatus status) {
        PropertyUnit unit = getUnitEntity(unitId);
        unit.setStatus(status);
        unitRepo.save(unit);
    }

    private PropertyUnitResponse mapToUnitResponse(PropertyUnit unit) {
        // We no longer get the tenant ID from the Unit entity directly.
        // If the UI needs the tenant ID, it should fetch it from the RentalProfile endpoint.
        return new PropertyUnitResponse(
                unit.getId(),
                unit.getUnitNumber(),
                unit.getRentAmount(),
                unit.getStatus(),
                null // Tenant ID is now managed by RentalProfile
        );
    }

    private PropertyResponse mapToPropertyResponse(Property p) {
        AddressResponseDTO addressDto = null;
        if (p.getAddress() != null) {
            Address a = p.getAddress();
            addressDto = new AddressResponseDTO(
                    a.getId(), a.getStreet(), a.getCity(), a.getStreet(), a.getZipCode(), a.getCountry()
            );
        }
        return new PropertyResponse(
                p.getId(),
                p.getName(),
                p.getLocation(),
                p.getNumberOfUnits(),
                addressDto,
                p.getUnits() != null ? p.getUnits().stream().map(this::mapToUnitResponse).toList() : List.of()
        );
    }
    // Inside PropertyService.java
    @Transactional(readOnly = true)
    public Optional<PropertyUnitResponse> getUnitByTenantOptional(UUID tenantId) {
        log.info("Querying active rental profiles for tenant ID: {}", tenantId);

        return rentalProfileRepo.findActiveByTenantId(tenantId)
                .map(profile -> {
                    // Safeguard against missing relationships
                    UUID unitId = (profile.getUnit() != null) ? profile.getUnit().getId() : profile.getId();
                    String unitNumber = (profile.getUnit() != null) ? profile.getUnit().getUnitNumber() : "UNASSIGNED";

                    // Map LeaseStatus to your record's UnitStatus if applicable, or pass a default conversion
                    com.xpro.rentalmain.rentalmain.model.UnitStatus viewStatus =
                            (profile.getStatus() == RentalProfile.LeaseStatus.ACTIVE)
                                    ? com.xpro.rentalmain.rentalmain.model.UnitStatus.OCCUPIED
                                    : UnitStatus.VACANT;

                    return new PropertyUnitResponse(
                            unitId,
                            unitNumber,
                            profile.getAgreedRentAmount(), // Maps perfectly to rentAmount DTO field
                            viewStatus,                    // Maps to UnitStatus status enum
                            tenantId                       // Maps to tenantId
                    );
                });
    }
}