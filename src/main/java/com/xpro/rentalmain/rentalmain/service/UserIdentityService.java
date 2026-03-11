package com.xpro.rentalmain.rentalmain.service;

import com.xpro.rentalmain.rentalmain.entity.UserIdentity;
import com.xpro.rentalmain.rentalmain.repository.UserIdentityRepository;
import jakarta.persistence.EntityNotFoundException;
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
