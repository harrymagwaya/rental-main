package com.xpro.rentalmain.rentalmain.service;

import com.xpro.rentalmain.rentalmain.dto.TenantCapacityRequestDTO;
import com.xpro.rentalmain.rentalmain.dto.UserRequest;
import com.xpro.rentalmain.rentalmain.dto.UserUpdateDTO;
import com.xpro.rentalmain.rentalmain.advice.ResourceAlreadyExistsException;
import com.xpro.rentalmain.rentalmain.dto.UserResponse;
import com.xpro.rentalmain.rentalmain.entity.Eligibility;
import com.xpro.rentalmain.rentalmain.entity.TenantCapacity;
import com.xpro.rentalmain.rentalmain.entity.User;
import com.xpro.rentalmain.rentalmain.model.RiskBand;
import com.xpro.rentalmain.rentalmain.model.UserStatus;
import com.xpro.rentalmain.rentalmain.model.UserType;
import com.xpro.rentalmain.rentalmain.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
@Slf4j
@Service
public class UserService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private  TenantCapacityService tenantCapacityService;

    @Lazy
    @Autowired
    private EligibilityService eligibilityService;

    @Autowired
    private BehavioralFeaturesService behavioralFeaturesService;



    // Helper method to convert Entity -> DTO (DRY Principle)
    private UserResponse mapToResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                user.getRole(),
                user.getGender(),
                user.getUserStatus()
        );
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(UserStatus status, Pageable pageable) {
        Page<User> users;
        if (status != null) {
            users = userRepository.findByUserStatus(status, pageable);
        } else {
            users = userRepository.findAll(pageable);
        }
        // Use .map() to convert each user in the page to a DTO
        return users.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public UserResponse findById(UUID userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return mapToResponse(user);
    }

    @Transactional(readOnly = true)
    public User getById(UUID userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return user;
    }

//    @Transactional
//    public UserResponse registerUser(UserRequest request, UUID actorId) {
//
//        if (actorId == null) {
//            throw new AccessDeniedException("Actor ID is missing. User must be authenticated.");
//        }
//        // 1. UNIQUE CHECKS (Email & Username)
//        if (userRepository.existsByEmail(request.getEmail())) {
//            throw new ResourceAlreadyExistsException("Email " + request.getEmail() + " is already in use.");
//        }
//        if (userRepository.existsByUsername(request.getUsername())) {
//            throw new ResourceAlreadyExistsException("Username " + request.getUsername() + " is already taken.");
//        }
//
//        // 2. AUTHORIZATION LOGIC
//        // We fetch the actor to check their permissions
//        User actor = userRepository.findById(actorId)
//                .orElseThrow(() -> new EntityNotFoundException("Actor not found"));
//
//        log.info("Actor {} is attempting to register a user with role: {}", actorId, request.getUserRole());
//
//        // Restriction: Only ADMIN or LANDLORD can create a TENANT
//            if (request.getUserRole() == UserType.TENANT) {
//            boolean isAuthorized = actor.getRole() == UserType.SYSTEM_ADMIN ||
//                    actor.getRole() == UserType.LANDLORD;
//
//            if (!isAuthorized) {
//                throw new AccessDeniedException("Insufficient permissions: Only Landlords or Admins can register tenants.");
//            }
//        }
//
//        // Restriction: Only ADMIN can create another ADMIN
//        if (request.getUserRole() == UserType.SYSTEM_ADMIN && actor.getRole() != UserType.SYSTEM_ADMIN) {
//            throw new AccessDeniedException("Only System Administrators can create Admin accounts.");
//        }
//
//        // 3. MAP AND SAVE
//        User user = User.builder()
//                .username(request.getUsername())
//                .email(request.getEmail())
//                .password(passwordEncoder.encode(request.getPassword()))
//                .firstName(request.getFirstName())
//                .lastName(request.getLastName())
//                .phoneNumber(request.getPhoneNumber())
//                .gender(request.getGender())
//                .role(request.getUserRole())
//                .userStatus(UserStatus.ACTIVE)
//                .createdBy(actorId)
//                .build();
//
//        return mapToResponse(userRepository.save(user));
//    }

    @Transactional
    public UserResponse registerUser(UserRequest request, UUID actorId) {
        // 1. UNIQUE CHECKS (Email & Username)
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException("Email " + request.getEmail() + " is already in use.");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResourceAlreadyExistsException("Username " + request.getUsername() + " is already taken.");
        }

        // 2. AUTHORIZATION & INITIALIZATION LOGIC
        UserType finalRole = request.getUserRole();
        UUID finalCreatedBy = actorId;

        long userCount = userRepository.count();

        if (userCount == 0) {
            // BOOTSTRAP MODE: First user becomes System Admin automatically
            log.info("No users found. Registering initial System Administrator: {}", request.getUsername());
            finalRole = UserType.SYSTEM_ADMIN;
            finalCreatedBy = null;
        } else {
            // STANDARD MODE: Require an actor and check permissions
            if (actorId == null) {
                throw new AccessDeniedException("Actor ID is missing. User must be authenticated to register others.");
            }

            User actor = userRepository.findById(actorId)
                    .orElseThrow(() -> new EntityNotFoundException("Actor not found"));

            log.info("Actor {} is attempting to register a user with role: {}", actorId, request.getUserRole());

            // Restriction: Only ADMIN or LANDLORD can create a TENANT
            if (request.getUserRole() == UserType.TENANT) {
                boolean isAuthorized = actor.getRole() == UserType.SYSTEM_ADMIN ||
                        actor.getRole() == UserType.LANDLORD;

                if (!isAuthorized) {
                    throw new AccessDeniedException("Insufficient permissions: Only Landlords or Admins can register tenants.");
                }
            }

            // Restriction: Only ADMIN can create another ADMIN
            if (request.getUserRole() == UserType.SYSTEM_ADMIN && actor.getRole() != UserType.SYSTEM_ADMIN) {
                throw new AccessDeniedException("Only System Administrators can create Admin accounts.");
            }
        }

        // 3. MAP AND SAVE USER IDENTITY
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .gender(request.getGender())
                .role(finalRole)
                .userStatus(UserStatus.ACTIVE)
                .createdBy(finalCreatedBy)
                .build();

        User savedUser = userRepository.save(user);

        // 4. DOWNSTREAM LIFECYCLE INITIALIZATION FIX
        // Automatically provision baseline records if the saved entity is a Tenant
        if (savedUser.getRole() == UserType.TENANT) {
            log.info("Initializing baseline credit and eligibility tracks for new Tenant: {}", savedUser.getId());

            // Populate Tenant Capacity Row with zero baselines
            TenantCapacityRequestDTO initialCapacity = new TenantCapacityRequestDTO(
                    savedUser.getId(),
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    false
            );
            tenantCapacityService.createOrUpdate(initialCapacity);

            try {
                behavioralFeaturesService.initializeDefaultTenantFeatures(savedUser.getId());
                eligibilityService.processFullEligibility(savedUser.getId());
                log.info("Successfully executed baseline full eligibility processing for tenant: {}", savedUser.getId());
            } catch (Exception e) {
                log.error("Failed to run baseline eligibility for user {}; tracking will catch up on next refresh.", savedUser.getId(), e);
            }
        }

        return mapToResponse(savedUser);
    }

    @Transactional
    public UserResponse updateUser(UUID userId, UserUpdateDTO updateDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (updateDto.getFirstName() != null) user.setFirstName(updateDto.getFirstName());
        if (updateDto.getLastName() != null) user.setLastName(updateDto.getLastName());
        if (updateDto.getPhoneNumber() != null) user.setPhoneNumber(updateDto.getPhoneNumber());
        if (updateDto.getGender() != null) user.setGender(updateDto.getGender());
        if (updateDto.getUserStatus() != null) user.setUserStatus(updateDto.getUserStatus());

        log.info("Updating profile for user: {}", userId);
        return mapToResponse(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        user.setUserStatus(UserStatus.DELETED);
        userRepository.save(user);

        log.info("User {} and associated profiles deactivated.", userId);
    }

    @Transactional(readOnly = true)
    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + email));
    }

    public List<User> getAllByRole(UserType role) {
        return userRepository.findAllByRole(role);
    }

    /**
     * EXTERNAL: Find by Email (DTO)
     * Used by Controllers to return data to the frontend
     */
    @Transactional(readOnly = true)
    public UserResponse findByEmail(String email) {
        return mapToResponse(getByEmail(email));
    }
}