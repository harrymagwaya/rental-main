package com.xpro.rentalmain.rentalmain.service;

import com.xpro.rentalmain.rentalmain.entity.UserIdentity;
import com.xpro.rentalmain.rentalmain.entity.UserProfile;
import com.xpro.rentalmain.rentalmain.repository.UserProfileRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class UserProfileService {

    @Autowired
    private UserIdentityService userIdentityService;

    @Autowired
    private UserProfileRepository profileRepository;

    public UserIdentity getUserIdentity(UUID id) {
        return userIdentityService.getUserById(id);
    }

    public UserProfile createUserProfile(UUID id) {
        // 1. Fetch the identity using your method
        UserIdentity identity = getUserIdentity(id);

        // 2. Build the profile using the identity's data
        UserProfile profile = UserProfile.builder()
                .id(identity.getId())         // Manually setting the @Id from identity
                .username(identity.getUsername())
                .email(identity.getEmail())
                .password(identity.getPassword())
                // Fields like firstName/lastName remain null until the user updates them
                .build();

        return profileRepository.save(profile);
    }

    @Transactional
    public UserProfile updateProfile(UUID id, UserProfile updateRequest) {
        // 1. Ensure the Identity still exists
        UserIdentity identity = getUserIdentity(id);

        // 2. Fetch current profile or create if it doesn't exist
        UserProfile profile = profileRepository.findById(id)
                .orElseGet(() -> UserProfile.builder().id(identity.getId()).build());

        // 3. Sync Identity fields (Always keep these in line with the Auth source)
        profile.setUsername(identity.getUsername());
        profile.setEmail(identity.getEmail());

        // 4. Update Profile-specific fields
        profile.setFirstName(updateRequest.getFirstName());
        profile.setLastName(updateRequest.getLastName());
        profile.setMiddleName(updateRequest.getMiddleName());
        profile.setDateOfBirth(updateRequest.getDateOfBirth());
        profile.setGender(updateRequest.getGender());
        profile.setHomeAddress(updateRequest.getHomeAddress());
        profile.setWorkAddress(updateRequest.getWorkAddress());

        return profileRepository.save(profile);
    }

    public UserProfile getProfileById(UUID id) {
        return profileRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found for ID: " + id));
    }




}
