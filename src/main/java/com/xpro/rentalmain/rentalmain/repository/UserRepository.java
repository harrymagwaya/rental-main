package com.xpro.rentalmain.rentalmain.repository;

import com.xpro.rentalmain.rentalmain.entity.User;

import com.xpro.rentalmain.rentalmain.model.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    // You might also need this if you want to check existence without fetching the whole object
    boolean existsByEmail(String email);




    boolean existsByUsername(String name);

    Page<User> findByUserStatus(UserStatus status, Pageable pageable);
}
