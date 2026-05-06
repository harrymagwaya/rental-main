package com.xpro.rentalmain.rentalmain.service;

import com.xpro.rentalmain.rentalmain.dto.RentalProfileCreateDTO;
import com.xpro.rentalmain.rentalmain.dto.RentalProfileResponseDTO;
import com.xpro.rentalmain.rentalmain.dto.RentalProfileUpdateDTO;
import com.xpro.rentalmain.rentalmain.entity.RentalProfile;
import com.xpro.rentalmain.rentalmain.entity.PropertyUnit;
import com.xpro.rentalmain.rentalmain.entity.Tenant;
import com.xpro.rentalmain.rentalmain.model.UnitStatus;
import com.xpro.rentalmain.rentalmain.repository.RentalProfileRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RentalProfileService {

    private final RentalProfileRepository repository;

    // Inject Services instead of Repositories
    private final TenantService tenantService;

    @Autowired
    @Lazy // Prevents circular dependency with PropertyService
    private final PropertyService propertyService;

    // --- CRUD: CREATE ---
    @Transactional
    public RentalProfileResponseDTO createProfile(RentalProfileCreateDTO dto) {
        // 1. Get Unit through PropertyService
        // Assuming you added a getUnitEntity method to PropertyService
        PropertyUnit unit = propertyService.getUnitEntity(dto.getUnitId());

        if (unit.getStatus() != UnitStatus.VACANT) {
            throw new IllegalStateException("Unit is not available for rent");
        }

        // 2. Get Tenant through TenantService
        // Assuming you have a getTenantEntity method in TenantService
        Tenant tenant = tenantService.getTenantEntity(dto.getTenantId());

        RentalProfile profile = RentalProfile.builder()
                .tenant(tenant)
                .unit(unit)
                .agreedRentAmount(dto.getAgreedRentAmount())
                .rentDueDay(dto.getRentDueDay())
                .leaseStartDate(dto.getLeaseStartDate())
                .leaseEndDate(dto.getLeaseEndDate())
                .status(RentalProfile.LeaseStatus.ACTIVE)
                .totalPayments(0)
                .totalLatePayments(0)
                .build();

        // 3. Use PropertyService to update status (encapsulates logic)
        propertyService.updateUnitStatus(unit.getId(), UnitStatus.OCCUPIED);

        return mapToResponseDTO(repository.save(profile));
    }

    // --- CRUD: READ (By ID) ---
    public RentalProfileResponseDTO getById(UUID id) {
        return repository.findById(id)
                .map(this::mapToResponseDTO)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found"));
    }

    // --- CRUD: READ (All) ---
    public List<RentalProfileResponseDTO> getAllProfiles() {
        return repository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    // --- CRUD: UPDATE ---
    @Transactional
    public RentalProfileResponseDTO updateProfile(UUID id, RentalProfileUpdateDTO dto) {
        RentalProfile profile = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found"));

        if (dto.getAgreedRentAmount() != null) profile.setAgreedRentAmount(dto.getAgreedRentAmount());
        if (dto.getRentDueDay() != null) profile.setRentDueDay(dto.getRentDueDay());
        if (dto.getLeaseEndDate() != null) profile.setLeaseEndDate(dto.getLeaseEndDate());

        if (dto.getStatus() != null) {
            RentalProfile.LeaseStatus newStatus = RentalProfile.LeaseStatus.valueOf(dto.getStatus());
            // If we are terminating an active lease via update
            if (newStatus == RentalProfile.LeaseStatus.TERMINATED && profile.getStatus() == RentalProfile.LeaseStatus.ACTIVE) {
                propertyService.updateUnitStatus(profile.getUnit().getId(), UnitStatus.VACANT);
            }
            profile.setStatus(newStatus);
        }

        return mapToResponseDTO(repository.save(profile));
    }

    // --- CRUD: DELETE ---
    @Transactional
    public void deleteProfile(UUID id) {
        RentalProfile profile = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found"));

        // Use property service to release the unit
        propertyService.updateUnitStatus(profile.getUnit().getId(), UnitStatus.VACANT);

        repository.delete(profile);
    }

    // --- EXTRA: Find by Tenant ---
    public List<RentalProfileResponseDTO> getByTenantId(UUID tenantId) {
        return repository.findByTenantId(tenantId).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    // --- EXTRA: Terminate Lease ---
    @Transactional
    public void terminateLease(UUID id) {
        RentalProfile profile = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found"));

        profile.setStatus(RentalProfile.LeaseStatus.TERMINATED);
        profile.setLeaseEndDate(LocalDate.now());

        // Notify PropertyService that the unit is now vacant
        propertyService.updateUnitStatus(profile.getUnit().getId(), UnitStatus.VACANT);

        repository.save(profile);
    }

    // --- MAPPING LOGIC ---
    private RentalProfileResponseDTO mapToResponseDTO(RentalProfile profile) {
        return RentalProfileResponseDTO.builder()
                .id(profile.getId())
                .tenantId(profile.getTenant().getId())
                .tenantName(profile.getTenant().getFirstName() + " " + profile.getTenant().getLastName())
                .unitId(profile.getUnit().getId())
                .unitNumber(profile.getUnit().getUnitNumber())
                .agreedRentAmount(profile.getAgreedRentAmount())
                .rentDueDay(profile.getRentDueDay())
                .leaseStartDate(profile.getLeaseStartDate())
                .leaseEndDate(profile.getLeaseEndDate())
                .status(profile.getStatus().name())
                .totalPayments(profile.getTotalPayments())
                .totalLatePayments(profile.getTotalLatePayments())
                .build();
    }
}