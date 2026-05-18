package com.xpro.rentalmain.rentalmain.service;

import com.xpro.rentalmain.rentalmain.dto.*;
import com.xpro.rentalmain.rentalmain.entity.Address;
import com.xpro.rentalmain.rentalmain.entity.LoanAdmin;
import com.xpro.rentalmain.rentalmain.entity.User;
import com.xpro.rentalmain.rentalmain.model.UserType;
import com.xpro.rentalmain.rentalmain.repository.LoanAdminRepository;
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
public class LoanAdminService {

    private final LoanAdminRepository adminRepo;
    private final UserService userService;
    private final AddressService addressService;

    /**
     * GET BY ID (Now backed by JIT Initialization)
     */
    @Transactional
    public LoanAdminResponseDTO getById(UUID userId) {
        // We delegate to the profile method to ensure consistent behavior
        return getAdminProfile(userId);
    }

    /**
     * GET PROFILE (The "Source of Truth" for Admin data)
     */
    @Transactional
    public LoanAdminResponseDTO getAdminProfile(UUID userId) {
        // 1. Verify master user exists first
        User masterUser = userService.getById(userId);

        // 2. Find or Initialize the Admin record
        LoanAdmin admin = adminRepo.findById(userId)
                .orElseGet(() -> {
                    log.info("JIT Initialization: Creating Loan Admin profile for user: {}", userId);
                    LoanAdmin newAdmin = LoanAdmin.builder()
                            .id(userId)
                            .createdAt(LocalDateTime.now())
                            .totalApprovals(0)
                            .totalRejections(0)
                            .build();
                    // Ensure it's persisted immediately
                    return adminRepo.save(newAdmin);
                });

        // 3. Sync fields (Email, Phone, Name from Master User)
        syncMasterFields(admin, masterUser);

        // 4. Save and Return
        return mapToResponse(adminRepo.save(admin));
    }

    /**
     * UPDATE ADMIN
     */
    @Transactional
    public LoanAdminResponseDTO updateAdmin(UUID userId, LoanAdminUpdateDTO dto) {
        getAdminProfile(userId); // Ensure it exists

        LoanAdmin existing = adminRepo.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Admin profile not found"));

        // Basic Profile updates
        if (dto.firstName() != null) existing.setFirstName(dto.firstName());
        if (dto.lastName() != null) existing.setLastName(dto.lastName());
        if (dto.email() != null) existing.setEmail(dto.email());
        if (dto.phoneNumber() != null) existing.setPhoneNumber(dto.phoneNumber());

        // Admin Specific updates
        if (dto.employeeId() != null) existing.setEmployeeId(dto.employeeId());
        if (dto.department() != null) existing.setDepartment(dto.department());
        if (dto.authorityLevel() != null) existing.setAuthorityLevel(dto.authorityLevel());

        // Handle Office Address via AddressService
        if (dto.officeAddress() != null) {
            if (existing.getOfficeAddress() != null) {
                addressService.updateAddress(existing.getOfficeAddress().getId(), dto.officeAddress());
            } else {

                // Convert UpdateDTO to RequestDTO
                var addrUpdate = dto.officeAddress();
                AddressRequest newRequest = new AddressRequest(
                        addrUpdate.street(),
                        addrUpdate.city(),
                        addrUpdate.country(),
                        addrUpdate.zipCode(),
                        addrUpdate.postalCode()
                );
                AddressResponse newAddr = addressService.createAddress(newRequest);
                existing.setOfficeAddress(addressService.getAddressEntity(newAddr.id()));
            }
        }

        existing.setUpdatedAt(LocalDateTime.now());
        return mapToResponse(adminRepo.save(existing));
    }


    //get all tenants
    @Transactional
    public List<LoanAdminResponseDTO> findAll() {
        log.info("Fetching all tenant profiles");

        // 1. Get all Users who have the LANDLORD role
        List<User> tenants = userService.getAllByRole(UserType.TENANT);

        // 2. Ensure each one has a profile record initialized
        tenants.forEach(user -> getAdminProfile(user.getId()));

        // 3. Now the Landlord table is populated, return the list
        return adminRepo.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * DELETE PROFILE
     */
    @Transactional
    public void deleteProfile(UUID userId) {
        if (!adminRepo.existsById(userId)) {
            throw new EntityNotFoundException("Loan Admin profile not found");
        }
        adminRepo.deleteById(userId);
        log.warn("Loan Admin profile deleted for user: {}", userId);
    }

    // --- HELPERS ---

    private void syncMasterFields(LoanAdmin admin, User user) {
        admin.setFirstName(user.getFirstName());
        admin.setLastName(user.getLastName());
        admin.setEmail(user.getEmail());
        admin.setPhoneNumber(user.getPhoneNumber());
    }

    private LoanAdminResponseDTO mapToResponse(LoanAdmin a) {
        AddressResponse addrDto = null;
        if (a.getOfficeAddress() != null) {
            Address addr = a.getOfficeAddress();
            addrDto = new AddressResponse(
                    addr.getId(), addr.getStreet(), addr.getCity(),
                    addr.getCountry(), addr.getZipCode(), addr.getPostalCode()
            );
        }

        return new LoanAdminResponseDTO(
                a.getId(), a.getFirstName(), a.getLastName(), a.getEmail(),
                a.getPhoneNumber(), a.getEmployeeId(), a.getDepartment(),
                a.getAuthorityLevel(), a.getTotalApprovals(), a.getTotalRejections(),
                addrDto, a.getCreatedAt()
        );
    }
}