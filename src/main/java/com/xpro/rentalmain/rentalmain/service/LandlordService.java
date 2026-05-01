package com.xpro.rentalmain.rentalmain.service;

import com.xpro.rentalmain.rentalmain.dto.AddressRequest;
import com.xpro.rentalmain.rentalmain.dto.AddressResponse;
import com.xpro.rentalmain.rentalmain.dto.LandlordResponseDTO;
import com.xpro.rentalmain.rentalmain.dto.LandlordUpdateDTO;
import com.xpro.rentalmain.rentalmain.entity.Address;
import com.xpro.rentalmain.rentalmain.entity.Landlord;
import com.xpro.rentalmain.rentalmain.entity.User;
import com.xpro.rentalmain.rentalmain.repository.AddressRepository;
import com.xpro.rentalmain.rentalmain.repository.LandlordRepository;
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
public class LandlordService {

    private final LandlordRepository landlordRepo;
    private final UserService userService;
    private final AddressService addressService;

    /**
     * GET OR INITIALIZE: Fetch the landlord profile or create it if the User exists.
     */
    @Transactional
    public LandlordResponseDTO getLandlordProfile(UUID userId) {
        User masterUser = userService.getById(userId);

        Landlord landlord = landlordRepo.findById(userId)
                .orElseGet(() -> {
                    log.info("Initializing Landlord profile for user: {}", userId);
                    return Landlord.builder()
                            .id(userId)
                            .build();
                });

        // Sync fields from Master Identity
        syncMasterFields(landlord, masterUser);

        return mapToResponse(landlordRepo.save(landlord));
    }

    /**
     * UPDATE LANDLORD (Null-Safe)
     */
    @Transactional
    public LandlordResponseDTO updateLandlord(UUID userId, LandlordUpdateDTO dto) {
        // Ensure profile exists
        getLandlordProfile(userId);

        Landlord existing = landlordRepo.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Landlord profile not found"));

        // Partial updates
        if (dto.firstName() != null) existing.setFirstName(dto.firstName());
        if (dto.lastName() != null) existing.setLastName(dto.lastName());
        if (dto.middleName() != null) existing.setMiddleName(dto.middleName());
        if (dto.email() != null) existing.setEmail(dto.email());
        if (dto.profilePhoto() != null) existing.setProfilePhoto(dto.profilePhoto());
        if (dto.dateOfBirth() != null) existing.setDateOfBirth(dto.dateOfBirth());
        if (dto.gender() != null) existing.setGender(dto.gender());


        // 3. Handle Home Address
        if (dto.homeAddress() != null) {
            if (existing.getHomeAddress() != null) {
                addressService.updateAddress(existing.getHomeAddress().getId(), dto.homeAddress());
            } else {

                // Convert UpdateDTO to RequestDTO
                var addrUpdate = dto.homeAddress();
                AddressRequest newRequest = new AddressRequest(
                        addrUpdate.street(),
                        addrUpdate.city(),
                        addrUpdate.country(),
                        addrUpdate.zipCode(),
                        addrUpdate.postalCode()
                );
                AddressResponse newAddr = addressService.createAddress(newRequest);
                existing.setHomeAddress(addressService.getAddressEntity(newAddr.id()));
            }
        }

        // 4. Handle Work Address
        if (dto.workAddress() != null) {
            if (existing.getWorkAddress() != null) {
                addressService.updateAddress(existing.getWorkAddress().getId(), dto.workAddress());
            } else {

                // Convert UpdateDTO to RequestDTO
                var addrUpdate = dto.workAddress();
                AddressRequest newRequest = new AddressRequest(
                        addrUpdate.street(),
                        addrUpdate.city(),
                        addrUpdate.country(),
                        addrUpdate.zipCode(),
                        addrUpdate.postalCode()
                );
                AddressResponse newAddr = addressService.createAddress(newRequest);
                existing.setWorkAddress(addressService.getAddressEntity(newAddr.id()));
            }
        }

        log.info("Updated Landlord profile for: {}", userId);
        return mapToResponse(landlordRepo.save(existing));
    }

    private void syncMasterFields(Landlord landlord, User user) {
        landlord.setFirstName(user.getFirstName());
        landlord.setLastName(user.getLastName());
        landlord.setEmail(user.getEmail());
        // Map any other User fields you need synced
    }

    /**
     * LIST ALL LANDLORDS
     * Useful for the Admin dashboard to see all registered owners.
     */
    @Transactional(readOnly = true)
    public List<LandlordResponseDTO> findAll() {
        log.info("Fetching all landlord profiles");
        return landlordRepo.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * DELETE LANDLORD PROFILE
     * This removes the Landlord extension but leaves the User identity intact.
     */
    @Transactional
    public void deleteLandlord(UUID userId) {
        if (!landlordRepo.existsById(userId)) {
            throw new EntityNotFoundException("Landlord profile not found for ID: " + userId);
        }

        // Note: You might want to check if they still own properties before deleting
        landlordRepo.deleteById(userId);
        log.warn("Landlord profile extension removed for user: {}", userId);
    }

    private LandlordResponseDTO mapToResponse(Landlord l) {
        return new LandlordResponseDTO(
                l.getId(),
                l.getFirstName(),
                l.getLastName(),
                l.getEmail(),
                l.getProfilePhoto(),
                l.getGender(),
                l.getDateOfBirth()
        );
    }


}