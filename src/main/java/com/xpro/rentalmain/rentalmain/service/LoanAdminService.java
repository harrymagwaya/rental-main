package com.xpro.rentalmain.rentalmain.service;

import com.xpro.rentalmain.rentalmain.dto.*;
import com.xpro.rentalmain.rentalmain.entity.Address;
import com.xpro.rentalmain.rentalmain.entity.LoanAdmin;
import com.xpro.rentalmain.rentalmain.entity.User;
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
     * GET PROFILE (JIT Initialization)
     */
    @Transactional
    public LoanAdminResponseDTO getAdminProfile(UUID userId) {
        User masterUser = userService.getById(userId);

        LoanAdmin admin = adminRepo.findById(userId)
                .orElseGet(() -> {
                    log.info("Initializing Loan Admin profile for user: {}", userId);
                    return LoanAdmin.builder()
                            .id(userId)
                            .createdAt(LocalDateTime.now())
                            .totalApprovals(0)
                            .totalRejections(0)
                            .build();
                });

        syncMasterFields(admin, masterUser);
        return mapToResponse(adminRepo.save(admin));
    }

    /**
     * GET BY ID (Explicit fetch)
     */
    @Transactional(readOnly = true)
    public LoanAdminResponseDTO getById(UUID userId) {
        return adminRepo.findById(userId)
                .map(this::mapToResponse)
                .orElseThrow(() -> new EntityNotFoundException("Loan Admin not found: " + userId));
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

    /**
     * LIST ALL
     */
    @Transactional(readOnly = true)
    public List<LoanAdminResponseDTO> findAll() {
        return adminRepo.findAll().stream().map(this::mapToResponse).toList();
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