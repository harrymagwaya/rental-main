package com.xpro.rentalmain.rentalmain.service;

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
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LandlordService {

    private final LandlordRepository landlordRepo;
    private final UserService userService;
    private final AddressRepository addressRepo;

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

        // Update Addresses
        if (dto.homeAddressId() != null) {
            Address home = addressRepo.findById(dto.homeAddressId())
                    .orElseThrow(() -> new EntityNotFoundException("Home Address not found"));
            existing.setHomeAddress(home);
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