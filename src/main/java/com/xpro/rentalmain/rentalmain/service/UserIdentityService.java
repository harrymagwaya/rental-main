package com.xpro.rentalmain.rentalmain.service;

import com.xpro.rentalmain.rentalmain.dto.CreateUserRequest;
import com.xpro.rentalmain.rentalmain.entity.UserIdentity;
import com.xpro.rentalmain.rentalmain.model.RentalApp;
import com.xpro.rentalmain.rentalmain.model.UserStatus;
import com.xpro.rentalmain.rentalmain.repository.UserIdentityRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class UserIdentityService {

    @Autowired
    private UserIdentityRepository userIdentityRepository;

    @Transactional
    public UserIdentity createUser(CreateUserRequest request, RentalApp app) {
        // 1. Validation
        if (userIdentityRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email is already registered");
        }

        // 2. Build User with App Context
        UserIdentity user = UserIdentity.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .userType(request.getUserType())

                // Set the App from the second argument
                .registeredFrom(app)

                // Lifecycle Defaults
                .status(UserStatus.REGISTERED)
                .enabled(true) // Allows login for REGISTERED/PENDING
                .build();

        return userRepository.save(user);
    }

    public UserIdentity getUserById(UUID id) {
        log.debug("Fetching user by id: {}", id);
        return userIdentityRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User with id {} not found", id);
                    return new EntityNotFoundException("User not found with id: " + id);
                });
    }

    public Optional<UserIdentity> findByEmail(String email) {
        log.debug("Finding user by email: {}", email);
        return userIdentityRepository.findByEmail(email);
    }





}
