package com.xpro.rentalmain.rentalmain.service;

import com.xpro.rentalmain.rentalmain.dto.AddressRequest;
import com.xpro.rentalmain.rentalmain.dto.AddressResponse;
import com.xpro.rentalmain.rentalmain.dto.TenantResponseDTO;
import com.xpro.rentalmain.rentalmain.dto.TenantUpdateDTO;
import com.xpro.rentalmain.rentalmain.entity.Address;
import com.xpro.rentalmain.rentalmain.entity.Tenant;
import com.xpro.rentalmain.rentalmain.entity.User;
import com.xpro.rentalmain.rentalmain.model.TenantStatus;
import com.xpro.rentalmain.rentalmain.repository.TenantRepository;
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
public class TenantService {

    private final TenantRepository tenantRepository;
    private final UserService userService; // Assuming this exists to fetch Master Identity
    private final AddressService addressService;

    /**
     * GET BY ID (With JIT Initialization)
     * Mirrors the HospitalAdmin logic: if the User exists but the Tenant profile
     * doesn't, we initialize it on the fly.
     */
    @Transactional
    public TenantResponseDTO getTenantProfile(UUID userId) {
        User masterUser = userService.getById(userId);

        Tenant tenant = tenantRepository.findById(userId)
                .orElseGet(() -> {
                    log.info("initializing Tenant profile for user: {}", userId);
                    Tenant newTenant = new Tenant();
                    newTenant.setId(userId);
                    newTenant.setCreatedAt(LocalDateTime.now());
                    newTenant.setStatus(TenantStatus.ACTIVE);
                    return newTenant;
                });

        // Sync fields from Master User to ensure the profile is up to date
        syncMasterFields(tenant, masterUser);

        return mapToResponse(tenantRepository.save(tenant));
    }

    /**
     * UPDATE TENANT (Null-Safe)
     */
    @Transactional
    public TenantResponseDTO updateTenant(UUID userId, TenantUpdateDTO dto) {
        // Use our JIT method to ensure the profile exists before updating
        getTenantProfile(userId);

        Tenant existing = tenantRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Tenant profile not found"));

        // Partial updates: only set if the DTO field is not null
        if (dto.firstName() != null) existing.setFirstName(dto.firstName());
        if (dto.lastName() != null) existing.setLastName(dto.lastName());
        if (dto.phoneNumber() != null) existing.setPhoneNumber(dto.phoneNumber());
        if (dto.email() != null) existing.setEmail(dto.email());
        if (dto.nationalId() != null) existing.setNationalId(dto.nationalId());
        if (dto.status() != null) existing.setStatus(dto.status());

        if (dto.address() != null) {
            if (existing.getPermanentAddress() != null) {
                // Update existing
                addressService.updateAddress(existing.getPermanentAddress().getId(), dto.address());
            } else {
                // Convert UpdateDTO to RequestDTO
                var addrUpdate = dto.address();
                AddressRequest newRequest = new AddressRequest(
                        addrUpdate.street(),
                        addrUpdate.city(),
                        addrUpdate.country(),
                        addrUpdate.zipCode(),
                        addrUpdate.postalCode()
                );

                // Create new address using the overloaded createAddress(AddressUpdateDTO)
                AddressResponse newAddr = addressService.createAddress(newRequest);
                existing.setPermanentAddress(addressService.getAddressEntity(newAddr.id()));
            }
        }

        existing.setUpdatedAt(LocalDateTime.now());

        log.info("Updated profile for tenant: {}", userId);
        return mapToResponse(tenantRepository.save(existing));
    }

    /**
     * LIST ALL TENANTS
     */
    @Transactional(readOnly = true)
    public List<TenantResponseDTO> findAll() {
        return tenantRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * DELETE PROFILE
     */
    @Transactional
    public void deleteProfile(UUID userId) {
        if (!tenantRepository.existsById(userId)) {
            throw new EntityNotFoundException("Cannot delete: Tenant profile does not exist");
        }
        tenantRepository.deleteById(userId);
        log.warn("Removed tenant profile extension for user: {}", userId);
    }

    // --- HELPERS ---

    private void syncMasterFields(Tenant tenant, User user) {
        tenant.setFirstName(user.getFirstName());
        tenant.setLastName(user.getLastName());
        tenant.setEmail(user.getEmail());
        tenant.setPhoneNumber(user.getPhoneNumber());
    }

    private TenantResponseDTO mapToResponse(Tenant t) {
        AddressResponse addressResponse = null;
        if (t.getPermanentAddress() != null) {
            Address a = t.getPermanentAddress();
            addressResponse = new AddressResponse(
                    a.getId(), a.getStreet(), a.getCity(),
                    a.getCountry(), a.getZipCode(), a.getPostalCode()
            );
        }

        return new TenantResponseDTO(
                t.getId(),
                t.getFirstName(),
                t.getLastName(),
                t.getPhoneNumber(),
                t.getEmail(),
                t.getNationalId(),
                t.getStatus(),
                t.getCreatedAt(),
                addressResponse // Include the full address in response
        );

}
}