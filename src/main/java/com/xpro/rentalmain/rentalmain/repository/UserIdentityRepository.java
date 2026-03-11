package com.xpro.rentalmain.rentalmain.repository;

import com.xpro.rentalmain.rentalmain.entity.UserIdentity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserIdentityRepository extends JpaRepository<UserIdentity, UUID> {

    Optional<UserIdentity> findByEmail(String email);

    // You might also need this if you want to check existence without fetching the whole object
    boolean existsByEmail(String email);
}
